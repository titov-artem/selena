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
     *                               if cluster manager can't return available hosts now due
     *                               to internal problems
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
     * Describe a listener with weight, witch provide ordering on listener. Not weighted listeners are supposed to have
     * weight equals to -infinity, so they will be executed at first in the order they were added.
     */
    public interface WeightedClusterEventListener extends ClusterEventListener {

        /**
         * Return weight of this listener. The most light listeners will be executed at first and the most hard
         * listeners at last. If weight is equals, then listeners will be executed in the order they were added
         *
         * @return integer weight
         */
        int getWeight();
    }

    /**
     * Describe an event in the cluster, such as loosing connection with cluster.
     */
    public static class ClusterEvent {
        private final ClusterEventType type;
        private final ClusterManager clusterManager;

        public ClusterEvent(final ClusterEventType type, final ClusterManager clusterManager) {
            this.type = type;
            this.clusterManager = clusterManager;
        }

        public ClusterEventType getType() {
            return type;
        }

        /**
         * Return cluster manager, which produce this event.
         *
         * @return cluster manager
         */
        public ClusterManager getClusterManager() {
            return clusterManager;
        }
    }

    /**
     * Describe type of cluster event.
     */
    public enum ClusterEventType {
        CLUSTER_CHANGED, CONNECTION_LOST
    }
}
