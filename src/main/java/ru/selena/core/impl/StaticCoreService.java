package ru.selena.core.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.CoreService;
import ru.selena.core.KeyRingService;
import ru.selena.net.model.Host;
import ru.selena.net.model.HostWithIntegerToken;

import java.util.Arrays;
import java.util.List;

/**
 * Date: 12/19/12
 * Time: 9:29 AM
 *
 * @author Artem Titov
 */
public class StaticCoreService implements CoreService, InitializingBean {

    private KeyRingService keyRingService;

    @Required
    public void setKeyRingService(KeyRingService keyRingService) {
        this.keyRingService = keyRingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        keyRingService.updateRing(Arrays.<Host>asList(
                new HostWithIntegerToken("localhost", 8080, 10000),
                new HostWithIntegerToken("localhost", 8081, 10002),
                new HostWithIntegerToken("localhost", 8082, 10004)
        ));
    }

    @Override
    public void joinCluster() {
        //todo implement method's body
    }

    @Override
    public void leaveCluster() {
        //todo implement method's body
    }

    @Override
    public Host getCurrentHost() {
        final int port = Integer.parseInt(System.getProperty("port"));
        return new HostWithIntegerToken("localhost", port, 10000);
    }

    @Override
    public List<Host> getAvailableHosts() {
        return null;  //todo implement method's body
    }
}
