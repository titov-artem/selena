package ru.selena.core;

import ru.selena.model.Key;
import ru.selena.net.model.Host;

import java.util.List;

/**
 * Date: 12/15/12
 * Time: 4:06 PM
 *
 * @author Artem Titov
 */
public interface KeyRingService {

    /**
     * Return hosts that serve this key. The first value in the list is a main replica and other are slave replicas
     *
     * @param key key
     * @return list of hosts
     */
    List<Host> getPreferredHosts(final Key key);

    /**
     * Return current list of replicas for specified host. The specified host is the first element in the list
     *
     * @param host host
     * @return not null not empty list with hosts replicas and itself s the first element.
     */
    List<Host> getReplicas(final Host host);

    /**
     * Update ring with new list of hosts.
     *
     * @param hosts hosts
     */
    void updateRing(final List<Host> hosts);

}
