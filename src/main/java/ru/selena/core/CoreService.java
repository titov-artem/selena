package ru.selena.core;

import ru.selena.net.model.Host;

import java.util.List;

/**
 * Date: 12/16/12
 * Time: 1:32 AM
 *
 * @author Artem Titov
 */
public interface CoreService {

    /**
     * Join cluster.
     */
    void joinCluster();

    /**
     * Leave cluster.
     */
    void leaveCluster();

    /**
     * Return current host object.
     *
     * @return current host
     */
    Host getCurrentHost();

    /**
     * Return list of active hosts in cluster.
     *
     * @return list of hosts
     */
    List<Host> getAvailableHosts();
}
