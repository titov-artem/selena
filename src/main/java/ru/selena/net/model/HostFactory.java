package ru.selena.net.model;

/**
 * Date: 3/5/13
 * Time: 12:35 PM
 *
 * @author Artem Titov
 */
public interface HostFactory {

    Host createHost(final String host, final int port, final byte[] token);
}
