package ru.selena.core.impl;

import ru.selena.core.ConfigurationService;

/**
 * Date: 12/23/12
 * Time: 4:24 PM
 *
 * @author Artem Titov
 */
public class StaticConfigurationService implements ConfigurationService {

    @Override
    public int getPort() {
        return Integer.parseInt(System.getProperty("port"));
    }

    @Override
    public String getHost() {
        return System.getProperty("host");
    }

    @Override
    public int getReplicationFactor() {
        return 3;
    }

    @Override
    public int getReadCount() {
        return 2;
    }

    @Override
    public int getWriteCount() {
        return 2;
    }
}
