package ru.selena.core.impl;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.*;
import ru.selena.core.exception.DataStoreException;
import ru.selena.core.exception.UpdatingOlderVersionException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.model.Host;
import ru.selena.utils.collections.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Date: 12/17/12
 * Time: 1:48 AM
 *
 * @author Artem Titov
 */
public final class CoordinationServiceImpl implements CoordinationService, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(CoordinationServiceImpl.class);

    private static final int MAX_RESPONSE_WAIT_TIMEOUT = 1000; //1s

    private int writeCount;
    private int readCount;
    private KeyRingService keyRingService;
    private LocalStoreService localStoreService;
    private RemoteStoreService remoteStoreService;
    private ClusterManager clusterManager;
    private Host currentHost;

    @Required
    public void setWriteCount(final int writeCount) {
        this.writeCount = writeCount;
    }

    @Required
    public void setReadCount(final int readCount) {
        this.readCount = readCount;
    }

    @Required
    public void setKeyRingService(final KeyRingService keyRingService) {
        this.keyRingService = keyRingService;
    }

    @Required
    public void setLocalStoreService(final LocalStoreService localStoreService) {
        this.localStoreService = localStoreService;
    }

    @Required
    public void setRemoteStoreService(final RemoteStoreService remoteStoreService) {
        this.remoteStoreService = remoteStoreService;
    }

    @Required
    public void setClusterManager(final ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.currentHost = clusterManager.getCurrentHost();
    }

    @Override
    public DataObject get(final Key key) throws DataStoreException {
        final CountDownLatch receivedResponses = new CountDownLatch(readCount);
        final AtomicReference<DataObject> responseContainer = new AtomicReference<DataObject>();
        final ReadResponseListener responseListener = new ReadResponseListener(
                receivedResponses, responseContainer
        );
        final ReadResponseHandlerThread handlerThread = new ReadResponseHandlerThread(
                key,
                new ReadOperationPerformer(),
                responseListener
        );
        handlerThread.start();
        boolean isComplete = false;
        try {
            isComplete = receivedResponses.await(MAX_RESPONSE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
        if (!isComplete) {
            throw new DataStoreException(
                    String.format("Read for %s failed. Not enough good responses received from cluster",
                            ArrayUtils.toHexString(key.getHash())));
        }
        final DataObject dataObject = responseContainer.get();
        if (dataObject == null) {
            throw new NoSuchElementException(ArrayUtils.toHexString(key.getHash()));
        }
        return dataObject;
    }

    @Override
    public void put(final DataObject dataObject) throws DataStoreException {
        final CountDownLatch receivedResponses = new CountDownLatch(writeCount);
        final AtomicReference<OperationResult> result = new AtomicReference<OperationResult>();
        final ResponseListener responseListener = new WriteResponseListener(result, receivedResponses);
        final WriteResponseHandlerThread handlerThread = new WriteResponseHandlerThread(
                dataObject,
                new WriteOperationPerformer(),
                responseListener
        );
        handlerThread.start();
        boolean isComplete = false;
        try {
            isComplete = receivedResponses.await(MAX_RESPONSE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
        if (!isComplete) {
            throw new DataStoreException(
                    String.format("Write for %s failed. Not enough good responses received from cluster",
                            ArrayUtils.toHexString(dataObject.getKey().getHash())));
        }
        if (result.get() == OperationResult.UPDATING_OLD_VERSION) {
            throw new UpdatingOlderVersionException("Trying to put older version than exist in the system");
        }
    }

    private class ReadResponseHandlerThread extends Thread {

        private final Key key;
        private final String keyHash;
        private final ReadOperationPerformer operationPerformer;
        private final ResponseListener responseListener;

        private ReadResponseHandlerThread(final Key key,
                                          final ReadOperationPerformer operationPerformer,
                                          final ResponseListener responseListener) {
            super("Reader_" + ArrayUtils.toHexString(key.getHash()) + "_thread");
            this.key = key;
            this.operationPerformer = operationPerformer;
            this.keyHash = ArrayUtils.toHexString(key.getHash());
            this.responseListener = responseListener;
        }

        @Override
        public void run() {
            log.debug(String.format("Start loading data for key[hash=%s]", keyHash));
            final List<Host> preferredHosts = keyRingService.getPreferredHosts(key);
            log.debug("Known replicas: " + preferredHosts);

            final BlockingQueue<ResponseWrapper> responses =
                    new ArrayBlockingQueue<ResponseWrapper>(preferredHosts.size());
            responseListener.init(preferredHosts);

            final ExecutorService executorService = Executors.newFixedThreadPool(preferredHosts.size());
            for (final Host host : preferredHosts) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Thread.currentThread().setName(getThreadName(host));
                        responses.offer(operationPerformer.perform(key, host));
                    }
                });
            }
            executorService.shutdown();

            while (true) {
                try {
                    final ResponseWrapper response = responses.poll(10, TimeUnit.MILLISECONDS);
                    if (response != null) {
                        responseListener.onResponse(response);
                    } else if (executorService.isTerminated()) {
                        break;
                    }
                } catch (InterruptedException ignore) {
                }
            }
            responseListener.finish();
        }

        private String getThreadName(final Host host) {
            return String.format("Thread-get-%s-%s", keyHash, host);
        }
    }

    private class WriteResponseHandlerThread extends Thread {

        private final DataObject dataObject;
        private final String keyHash;
        private final WriteOperationPerformer operationPerformer;
        private final ResponseListener responseListener;

        private WriteResponseHandlerThread(final DataObject dataObject,
                                           final WriteOperationPerformer operationPerformer,
                                           final ResponseListener responseListener) {
            super("Writer_" + ArrayUtils.toHexString(dataObject.getKey().getHash()) + "_thread");
            this.dataObject = dataObject;
            this.operationPerformer = operationPerformer;
            this.responseListener = responseListener;
            this.keyHash = ArrayUtils.toHexString(dataObject.getKey().getHash());
        }

        @Override
        public void run() {
            log.debug(String.format("Start storing data for key[hash=%s], version=%s", keyHash, dataObject.getVersion()));
            final List<Host> preferredHosts = keyRingService.getPreferredHosts(dataObject.getKey());
            log.debug("Known replicas: " + preferredHosts);

            final BlockingQueue<ResponseWrapper> responses =
                    new ArrayBlockingQueue<ResponseWrapper>(preferredHosts.size());
            responseListener.init(preferredHosts);

            boolean needContinue = true;
            for (final Host host : preferredHosts) {
                if (currentHost.equals(host)) {
                    final ResponseWrapper res = operationPerformer.perform(dataObject, host);
                    responses.offer(res);
                    if (res.getResult() == OperationResult.UPDATING_OLD_VERSION) {
                        needContinue = false;
                    }
                    break;
                }
            }

            final ExecutorService executorService = Executors.newFixedThreadPool(preferredHosts.size());
            if (needContinue) {
                for (final Host host : preferredHosts) {
                    if (!currentHost.equals(host)) {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                Thread.currentThread().setName(getThreadName(host));
                                responses.offer(operationPerformer.perform(dataObject, host));
                            }
                        });
                    }
                }
            }
            executorService.shutdown();

            while (true) {
                try {
                    final ResponseWrapper response = responses.poll(10, TimeUnit.MILLISECONDS);
                    if (response != null) {
                        responseListener.onResponse(response);
                    } else if (executorService.isTerminated()) {
                        break;
                    }
                } catch (InterruptedException ignore) {
                }
            }
            responseListener.finish();
        }

        private String getThreadName(final Host host) {
            return String.format("Thread-get-%s-%s", keyHash, host);
        }
    }

    private static final class ResponseWrapper {
        private final Host host;
        private final OperationResult result;
        private final DataObject data;

        private ResponseWrapper(final Host host, final OperationResult result, final DataObject data) {
            this.data = data;
            this.result = result;
            this.host = host;
        }

        public Host getHost() {
            return host;
        }

        public OperationResult getResult() {
            return result;
        }

        public DataObject getData() {
            return data;
        }
    }

    private static enum OperationResult {
        SUCCESS, ERROR, NO_SUCH_ELEMENT, UPDATING_OLD_VERSION
    }

    private static interface OperationPerformer<T> {
        ResponseWrapper perform(final T t, final Host host);
    }

    private static interface ResponseListener {
        void init(final List<Host> hosts);

        void onResponse(final ResponseWrapper response);

        void finish();
    }

    private class ReadOperationPerformer implements OperationPerformer<Key> {

        @Override
        public ResponseWrapper perform(final Key key, final Host host) {
            log.debug("Loading data from " + host);
            DataObject dataObject = null;
            OperationResult result;
            try {
                if (currentHost.equals(host)) {
                    log.debug("This is current host. Use local storage service");
                    dataObject = localStoreService.get(key);
                } else {
                    log.debug("This is remote host. Use remote storage service");
                    dataObject = remoteStoreService.get(key, host);
                }
                result = OperationResult.SUCCESS;
            } catch (DataStoreException e) {
                final String keyHash = ArrayUtils.toHexString(key.getHash());
                log.error("Failed to get data for key[hash=" + keyHash + "]", e);
                result = OperationResult.ERROR;
            } catch (NoSuchElementException ignore) {
                log.warn("Missing data on the replica: " + host);
                result = OperationResult.NO_SUCH_ELEMENT;
            }
            return new ResponseWrapper(host, result, dataObject);
        }
    }

    private class WriteOperationPerformer implements OperationPerformer<DataObject> {
        @Override
        public ResponseWrapper perform(final DataObject dataObject, final Host host) {
            OperationResult result;
            try {
                if (!currentHost.equals(host)) {
                    remoteStoreService.put(dataObject, host);
                } else {
                    localStoreService.put(dataObject);
                }
                result = OperationResult.SUCCESS;
            } catch (UpdatingOlderVersionException e) {
                log.warn("Trying to put older version than exist in the system", e);
                result = OperationResult.UPDATING_OLD_VERSION;
            } catch (DataStoreException e) {
                final String keyHash = ArrayUtils.toHexString(dataObject.getKey().getHash());
                log.error(String.format("Remote store operation failed for key[hash=%s]", keyHash), e);
                result = OperationResult.ERROR;
            }
            return new ResponseWrapper(host, result, dataObject);
        }
    }

    private class ReadResponseListener implements ResponseListener {

        private final CountDownLatch receivedResponses;
        private final AtomicReference<DataObject> newestDataObject;
        private final Set<Host> hostWithNewestObjects;
        private List<Host> prefferedHosts;

        private ReadResponseListener(final CountDownLatch receivedResponses,
                                     final AtomicReference<DataObject> newestDataObject) {
            this.receivedResponses = receivedResponses;
            this.newestDataObject = newestDataObject;
            this.hostWithNewestObjects = new HashSet<Host>();
        }

        @Override
        public void init(final List<Host> hosts) {
            prefferedHosts = hosts;
        }

        @Override
        public void onResponse(final ResponseWrapper response) {
            final DataObject remoteObject = response.getData();
            final Host remoteHost = response.getHost();
            final OperationResult result = response.getResult();
            log.debug(String.format("Host: %s; Result: %s; Data: %s;",
                    remoteHost, result, remoteObject != null ? remoteObject.getVersion() : "null"));
            if (result == OperationResult.ERROR) {
                return;
            }
            if (result == OperationResult.NO_SUCH_ELEMENT) {
                receivedResponses.countDown();
                return;
            }
            if (newestDataObject.get() == null) {
                newestDataObject.set(remoteObject);
                hostWithNewestObjects.add(remoteHost);
            } else {
                // here we've already got one not null response
                assert remoteObject != null;
                if (remoteObject.getVersion().equals(newestDataObject.get().getVersion())) {
                    hostWithNewestObjects.add(remoteHost);
                } else if (remoteObject.getVersion().isAfter(newestDataObject.get().getVersion())) {
                    newestDataObject.set(remoteObject);
                    hostWithNewestObjects.clear();
                    hostWithNewestObjects.add(remoteHost);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Current newest object is " +
                        (newestDataObject.get() != null ? newestDataObject.get().getVersion() : "null"));
                log.debug("Current replicas with newest object: " + hostWithNewestObjects);
            }
            receivedResponses.countDown();
        }

        @Override
        public void finish() {
            if (newestDataObject.get() != null && hostWithNewestObjects.size() != prefferedHosts.size()) {
                readRepair(newestDataObject.get(), hostWithNewestObjects, prefferedHosts);
            }
        }

        /**
         * Perform write operation for those host which data object version was older than the newest one.
         *
         * @param newestDataObject newest data object
         * @param bestHosts        hosts with the newest data object
         * @param hosts            all replica hosts
         */
        private void readRepair(final DataObject newestDataObject,
                                final Set<Host> bestHosts,
                                final List<Host> hosts) {
            Validate.notNull(newestDataObject, "Newest data object can't be null");

            log.debug("Performing read repair. Newest version is " + newestDataObject.getVersion());
            final String keyHash = ArrayUtils.toHexString(newestDataObject.getKey().getHash());
            log.debug("" + (hosts.size() - bestHosts.size()));
            final ExecutorService executorService = Executors.newFixedThreadPool(hosts.size() - bestHosts.size());
            for (final Host host : hosts) {
                if (!bestHosts.contains(host)) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName(String.format("Thread-repair-%s-%s", keyHash, host));
                            try {
                                if (host.equals(currentHost)) {
                                    localStoreService.put(newestDataObject);
                                } else {
                                    remoteStoreService.put(newestDataObject, host);
                                }
                            } catch (DataStoreException e) {
                                log.error("Repair failed for host " + host, e);
                            }
                        }
                    });
                }
            }
            executorService.shutdown();
            try {
                while (!executorService.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                }
            } catch (InterruptedException ignore) {
                executorService.shutdownNow();
            }
            log.debug("Repairing complete");
        }
    }

    private class WriteResponseListener implements ResponseListener {

        private final AtomicReference<OperationResult> result;
        private final CountDownLatch receivedResponses;
        private DataObject dataObject;

        private WriteResponseListener(final AtomicReference<OperationResult> result,
                                      final CountDownLatch receivedResponses) {
            this.result = result;
            this.receivedResponses = receivedResponses;
        }

        @Override
        public void init(final List<Host> hosts) {
        }

        @Override
        public void onResponse(final ResponseWrapper response) {
            final OperationResult remoteOperationResult = response.getResult();
            final Host remoteHost = response.getHost();
            dataObject = response.getData();
            log.debug(String.format("Got response from remote host %s: %s",
                    remoteHost, remoteOperationResult));
            if (remoteOperationResult == OperationResult.SUCCESS) {
                receivedResponses.countDown();
            } else if (remoteOperationResult == OperationResult.UPDATING_OLD_VERSION) {
                result.set(OperationResult.UPDATING_OLD_VERSION);
                for (long i = 0; i < receivedResponses.getCount(); i++) {
                    receivedResponses.countDown();
                }
            }
        }

        @Override
        public void finish() {
            if (result.get() == OperationResult.UPDATING_OLD_VERSION) {
                try {
                    CoordinationServiceImpl.this.get(dataObject.getKey());
                } catch (DataStoreException e) {
                    log.error("Failed to perform repair write", e);
                }
            }
        }
    }
}
