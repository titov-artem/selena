package ru.selena.net.model.impl;

import ru.selena.net.model.Host;
import ru.selena.net.model.HostFactory;
import ru.selena.util.NumberUtils;

/**
 * Date: 3/5/13
 * Time: 12:38 PM
 *
 * @author Artem Titov
 */
public class HostWithIntegerTokenFactory implements HostFactory {
    @Override
    public Host createHost(final String host, final int port, final byte[] token) {
        return new HostWithIntegerToken(host, port, NumberUtils.toInt(token));
    }
}
