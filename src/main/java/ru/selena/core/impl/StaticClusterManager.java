package ru.selena.core.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.Factories;
import ru.selena.core.ClusterManager;
import ru.selena.core.KeyRingService;
import ru.selena.net.model.Host;
import ru.selena.net.model.impl.HostWithIntegerToken;
import ru.selena.net.model.impl.HostWithIntegerTokenFactory;
import ru.selena.util.NumberUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Date: 12/19/12
 * Time: 9:29 AM
 *
 * @author Artem Titov
 */
public class StaticClusterManager implements ClusterManager, InitializingBean {

    private KeyRingService keyRingService;

    @Required
    public void setKeyRingService(KeyRingService keyRingService) {
        this.keyRingService = keyRingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        keyRingService.updateRing(Arrays.<Host>asList(
                new HostWithIntegerTokenFactory().createHost("localhost", 8080, NumberUtils.toByteArray(10000)),
                new HostWithIntegerTokenFactory().createHost("localhost", 8081, NumberUtils.toByteArray(10002)),
                new HostWithIntegerTokenFactory().createHost("localhost", 8082, NumberUtils.toByteArray(10004))
        ));
    }

    @Override
    public Host joinCluster() {
        return null;
        //todo implement method's body
    }

    @Override
    public void leaveCluster() {
        //todo implement method's body
    }

    @Override
    public Host getCurrentHost() {
        final int port = Integer.parseInt(System.getProperty("port"));
        return new HostWithIntegerTokenFactory().createHost("localhost", port, NumberUtils.toByteArray(10000));
    }

    @Override
    public List<Host> getAvailableHosts() {
        return null;  //todo implement method's body
    }

    @Override
    public void setClusterEventListeners(final Iterable<ClusterEventListener> listener) {
        // do nothing
    }
}
