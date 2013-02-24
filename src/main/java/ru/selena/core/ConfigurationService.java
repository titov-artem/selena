package ru.selena.core;

/**
 * Date: 12/21/12
 * Time: 12:56 PM
 *
 * @author Artem Titov
 */
public interface ConfigurationService {

    int getPort();

    String getHost();

    int getReplicationFactor();

    int getReadCount();

    int getWriteCount();
}
