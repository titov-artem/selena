package ru.selena.core.impl;

import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.KeyRingService;
import ru.selena.model.Key;
import ru.selena.net.model.Host;
import ru.selena.utils.NumberUtils;
import ru.selena.utils.collections.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Assumes that token is integer number.
 * <p/>
 * Date: 12/16/12
 * Time: 1:57 AM
 *
 * @author Artem Titov
 */
public class IntegerKeyRingService implements KeyRingService {

    private static final Comparator<Pair<Integer, Host>> RING_ELEMENT_COMPARATOR = new Comparator<Pair<Integer, Host>>() {
        @Override
        public int compare(final Pair<Integer, Host> o1, final Pair<Integer, Host> o2) {
            return o1.getFirst().compareTo(o2.getFirst());
        }
    };

    private int replicationFactor;
    private ReadWriteLock lock;
    private List<Pair<Integer, Host>> ring;

    public IntegerKeyRingService() {
        lock = new ReentrantReadWriteLock();
        ring = new ArrayList<Pair<Integer, Host>>(0);
    }

    @Required
    public void setReplicationFactor(final int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    @Override
    public List<Host> getPreferredHosts(final Key key) {
        return getPreferredHostsByHash(key.getHash());
    }

    @Override
    public List<Host> getReplicas(final Host host) {
        return getPreferredHostsByHash(host.getToken());
    }

    private List<Host> getPreferredHostsByHash(final byte[] rawHash) {
        final int hash = NumberUtils.toInt(rawHash);

        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            final int pos = Collections.binarySearch(ring, Pair.of(hash, (Host) null), RING_ELEMENT_COMPARATOR);
            if (pos > 0) {
                return getHostWithReplicas(pos);
            } else {
                return getHostWithReplicas(-(pos + 1));
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Return {@code replicationFactor} hosts in the ring starting from {@code pos}.
     *
     * @param pos start position
     * @return hosts
     */
    private List<Host> getHostWithReplicas(final int pos) {
        final List<Host> hosts = new ArrayList<Host>(replicationFactor);
        final Pair<Integer, Host> mainReplica = ring.get(pos);
        hosts.add(mainReplica.getSecond());
        for (int i = 1; i < replicationFactor; i++) {
            final int index = (i + pos) % ring.size();
            if (mainReplica.equals(ring.get(index))) {
                break;
            }
            hosts.add(ring.get(index).getSecond());

        }
        return hosts;
    }

    @Override
    public void updateRing(final List<Host> hosts) {
        final List<Pair<Integer, Host>> newRing = new ArrayList<Pair<Integer, Host>>(hosts.size());
        for (final Host host : hosts) {
            newRing.add(Pair.of(NumberUtils.toInt(host.getToken()), host));
        }
        Collections.sort(newRing, RING_ELEMENT_COMPARATOR);
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            this.ring = newRing;
        } finally {
            writeLock.unlock();
        }
    }
}
