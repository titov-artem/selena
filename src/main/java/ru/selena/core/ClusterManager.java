package ru.selena.core;

import ru.selena.core.exception.ClusterConnectionException;
import ru.selena.net.model.Host;

import java.util.List;

/**
 * Date: 12/16/12
 * Time: 1:32 AM
 *
 * @author Artem Titov
 */
public interface ClusterManager {

    /**
     * Join cluster.
     *
     * @return current host object
     */
    Host joinCluster() throws ClusterConnectionException;

    /**
     * Leave cluster.
     *
     * @throws IllegalStateException if cluster manager doesn't connected to cluster
     */
    void leaveCluster();

    /**
     * Return current host object.
     *
     * @return current host
     * @throws IllegalStateException if cluster manager doesn't connected to cluster
     */
    Host getCurrentHost();

    /**
     * Return list of active hosts in cluster.
     *
     * @return list of hosts
     * @throws ru.selena.core.exception.OperationFailedException
     *          if cluster manager can't return available hosts now due
     *          to internal problems
     * @throws IllegalStateException if cluster manager doesn't connected to cluster
     */
    List<Host> getAvailableHosts();

    /**
     * Set listeners for cluster event. When any even acquires listeners will be invoked sequentially. If any one will
     * fail, than it will be skipped and invocation will continue.
     *
     * @param listeners listeners
     */
    void setClusterEventListeners(final Iterable<ClusterEventListener> listeners);

    /**
     * Describe a listener interface for cluster events.
     */
    public interface ClusterEventListener {
        void onCLusterEvent(final ClusterEvent event);
    }

    /**
     * Describe an event in the cluster, such as loosing connection with cluster.
     */
    public static class ClusterEvent {
        private final ClusterEventType type;

        public ClusterEvent(final ClusterEventType type) {
            this.type = type;
        }

        public ClusterEventType getType() {
            return type;
        }
    }

    /**
     * Describe type of cluster event.
     */
    public enum ClusterEventType {
        CLUSTER_CHANGED, CONNECTION_LOST
    }
}
