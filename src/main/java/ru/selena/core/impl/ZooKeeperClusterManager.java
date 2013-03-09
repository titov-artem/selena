package ru.selena.core.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.Factories;
import ru.selena.core.ClusterManager;
import ru.selena.core.HostTokenGenerator;
import ru.selena.core.exception.ClusterConnectionException;
import ru.selena.core.exception.OperationFailedException;
import ru.selena.net.model.Host;
import ru.selena.utils.collections.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Date: 2/27/13
 * Time: 10:15 PM
 *
 * @author Artem Titov
 */
public class ZooKeeperClusterManager implements ClusterManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionWatcher.class);

    private static final Function<String, Host> DECODER_FUNCTION = new Function<String, Host>() {
        @Override
        public Host apply(final String source) {
            if (source == null) {
                throw new IllegalArgumentException("Host string can't be null");
            }
            final String[] data = source.split(":");
            if (data.length != 3) {
                throw new IllegalArgumentException("Wrong format: expected <host>:<port>:<hexadecimal key>, but got " + source);
            }
            try {
                return Factories.Instances.getHostFactory().createHost(data[0], Integer.parseInt(data[1]), ArrayUtils.toByteArray(data[2]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Wrong format: expected <host>:<port>:<hexadecimal key>, but got " + source);
            }
        }
    };
    private static final Function<Host, byte[]> HOST_TO_TOKEN = new Function<Host, byte[]>() {
        @Override
        public byte[] apply(final Host arg) {
            return arg.getToken();
        }
    };
    private static final Predicate<Object> IS_WEIGHTED = Predicates.instanceOf(WeightedClusterEventListener.class);
    private static final Function<ClusterEventListener, WeightedClusterEventListener> TO_WEIGHTED =
            new Function<ClusterEventListener, WeightedClusterEventListener>() {
                @Override
                public WeightedClusterEventListener apply(final ClusterEventListener arg) {
                    return (WeightedClusterEventListener) arg;
                }
            };

    private static final int RETRY_COUNT = 3;
    private static final long DELAY_TIMEOUT_MS = 1000;
    private ZooKeeper zooKeeper;
    private ConnectionWatcher connectionWatcher;
    private Host currentHost;

    private String zooKeeperConnectionString;
    private int zooKeeperSessionTimeout;
    private String zooKeeperFolder;
    private String currentHostName;
    private int currentPort;

    private HostTokenGenerator hostTokenGenerator;
    private Iterable<ClusterEventListener> listeners;
    private Iterable<WeightedClusterEventListener> weightedListeners;

    @Required
    public void setZooKeeperConnectionString(final String zooKeeperConnectionString) {
        this.zooKeeperConnectionString = zooKeeperConnectionString;
    }

    @Required
    public void setZooKeeperSessionTimeout(final int zooKeeperSessionTimeout) {
        this.zooKeeperSessionTimeout = zooKeeperSessionTimeout;
    }

    @Required
    public void setZooKeeperFolder(final String zooKeeperFolder) {
        this.zooKeeperFolder = zooKeeperFolder;
    }

    @Required
    public void setCurrentHostName(final String currentHostName) {
        this.currentHostName = currentHostName;
    }

    @Required
    public void setCurrentPort(final int currentPort) {
        this.currentPort = currentPort;
    }

    @Required
    public void setHostTokenGenerator(final HostTokenGenerator hostTokenGenerator) {
        this.hostTokenGenerator = hostTokenGenerator;
    }

    @Override
    public void setClusterEventListeners(final Iterable<ClusterEventListener> listeners) {
        this.listeners = Iterables.filter(listeners, Predicates.not(IS_WEIGHTED));
        final ArrayList<WeightedClusterEventListener> weightedListeners = Lists.newArrayList(
                Iterables.transform(Iterables.filter(listeners, IS_WEIGHTED), TO_WEIGHTED)
        );
        Collections.sort(weightedListeners, new Comparator<WeightedClusterEventListener>() {
            @Override
            public int compare(final WeightedClusterEventListener o1, final WeightedClusterEventListener o2) {
                return o1.getWeight() - o2.getWeight();
            }
        });
        this.weightedListeners = weightedListeners;
    }

    private void connectToZooKeeper() throws IOException, InterruptedException {
        final CountDownLatch connectionEstablished = new CountDownLatch(1);
        connectionWatcher = new ConnectionWatcher(connectionEstablished);
        zooKeeper = new ZooKeeper(zooKeeperConnectionString, zooKeeperSessionTimeout, connectionWatcher);
        connectionEstablished.await();
    }

    @Override
    public Host joinCluster() throws ClusterConnectionException {
        try {
            connectToZooKeeper();
        } catch (IOException e) {
            throw new ClusterConnectionException("Failed to connect to ZooKeeper", e);
        } catch (InterruptedException e) {
            throw new ClusterConnectionException("Connecting to ZooKeeper was interrupted", e);
        }
        final List<Host> availableHosts = getAvailableHosts();
        final byte[] currentToken = hostTokenGenerator.generateToken(Lists.transform(availableHosts, HOST_TO_TOKEN));
        currentHost = Factories.Instances.getHostFactory().createHost(currentHostName, currentPort, currentToken);
        return currentHost;
    }

    @Override
    public void leaveCluster() {
        checkIsConnected();
        try {
            zooKeeper.close();
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public Host getCurrentHost() {
        checkIsConnected();
        return currentHost;
    }

    @Override
    public List<Host> getAvailableHosts() {
        checkIsConnected();
        return doActionWithRetry(new Action<List<Host>>() {
            @Override
            public List<Host> act() throws InterruptedException, KeeperException {
                final List<String> hostInfos = zooKeeper.getChildren(zooKeeperFolder, connectionWatcher);
                return Lists.transform(hostInfos, DECODER_FUNCTION);
            }
        }, false);
    }

    private void checkIsConnected() {
        if (zooKeeper == null) {
            throw new IllegalStateException("Cluster manager doesn't connected to cluster");
        }
    }

    /**
     * @param action
     * @param canBeInterrupt
     * @param <T>
     * @return result of if canBeInterrupt == true null if thread was interrupted.
     */
    private <T> T doActionWithRetry(final Action<T> action, final boolean canBeInterrupt) {
        Exception cause = null;
        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
            try {
                return action.act();
            } catch (KeeperException.SessionExpiredException e) {
                throw new IllegalStateException("Lock lost");
            } catch (KeeperException e) {
                cause = e;
            } catch (InterruptedException e) {
                if (canBeInterrupt) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            if (attempt != RETRY_COUNT) {
                try {
                    Thread.sleep(DELAY_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    if (canBeInterrupt) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        if (cause != null) {
            throw new OperationFailedException("Failed to perform action due to exception: " + cause.getMessage(), cause);
        } else {
            throw new AssertionError("Failed to perform action due but the problem is unknown");
        }
    }

    /**
     * Dispatch event to all listeners. If any exception occurs while processing, then error message would be written
     * to the log and next listener will be processed.
     * <p/>
     * At first event will be processed by not weighted event listeners and after that by weighted event listeners in
     * order or weight increasing
     *
     * @param clusterEvent cluster event
     */
    private void processEvent(final ClusterEvent clusterEvent) {
        for (final ClusterEventListener listener : listeners) {
            try {
                listener.onCLusterEvent(clusterEvent);
            } catch (RuntimeException e) {
                final String message = String.format("Listener %s failed with error: %s",
                        listener.getClass().getCanonicalName(),
                        e.getMessage());
                log.error(message, e);
            }
        }
        for (final WeightedClusterEventListener listener : weightedListeners) {
            try {
                listener.onCLusterEvent(clusterEvent);
            } catch (RuntimeException e) {
                final String message = String.format("Listener %s failed with error: %s",
                        listener.getClass().getCanonicalName(),
                        e.getMessage());
                log.error(message, e);
            }
        }
    }

    private class ConnectionWatcher implements Watcher {

        private CountDownLatch connectionEstablished;

        public ConnectionWatcher(final CountDownLatch connectionEstablished) {
            this.connectionEstablished = connectionEstablished;
        }

        @Override
        public void process(final WatchedEvent event) {
            final Event.EventType type = event.getType();
            final Event.KeeperState state = event.getState();
            switch (state) {
                case Expired: {
                    processEvent(new ClusterEvent(ClusterEventType.CONNECTION_LOST, ZooKeeperClusterManager.this));
                }
                break;
                case SyncConnected: {
                    connectionEstablished.countDown();
                    switch (type) {
                        case NodeChildrenChanged: {
                            processEvent(new ClusterEvent(ClusterEventType.CLUSTER_CHANGED, ZooKeeperClusterManager.this));
                        }
                        break;
                        default: {// do nothing
                        }
                    }
                }
                break;
                case Disconnected: {
                }
                break;
                default: {//do nothing
                }
            }
        }
    }

    private interface Action<T> {
        public T act() throws InterruptedException, KeeperException;
    }

    private static abstract class ActionWithoutResult implements Action<Object> {

        @Override
        public Object act() throws InterruptedException, KeeperException {
            doAct();
            return null;
        }

        public abstract void doAct() throws InterruptedException, KeeperException;
    }

}
