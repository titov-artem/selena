package ru.selena.net.model.impl;

import org.apache.commons.lang.Validate;
import ru.selena.net.model.Host;
import ru.selena.utils.NumberUtils;

/**
 * Assumes that token is a integer number.
 * <p/>
 * Date: 12/16/12
 * Time: 2:19 PM
 *
 * @author Artem Titov
 */
public class HostWithIntegerToken implements Host {

    private final String host;
    private final int port;
    private final int token;
    private final int calculatedHashCode;

    HostWithIntegerToken(final String host, final int port, final int token) {
        Validate.isTrue(host != null && !host.isEmpty(), "Host can't be null or empty string");
        this.host = host;
        this.port = port;
        this.token = token;
        this.calculatedHashCode = calculateHashCode();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public Integer getPort1() {
        return port;
    }

    @Override
    public byte[] getToken() {
        return NumberUtils.toByteArray(token);
    }

    @Override
    public int hashCode() {
        return calculatedHashCode;
    }

    private int calculateHashCode() {
        int result = 17;
        result += 31 * result + host.hashCode();
        result += 31 * result + new Integer(port).hashCode();
        result += 31 * result + new Integer(token).hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HostWithIntegerToken)) {
            return false;
        }
        final HostWithIntegerToken h = (HostWithIntegerToken) obj;
        return host.equals(h.host) && port == h.port && token == h.token;
    }

    @Override
    public String toString() {
        return String.format("%s:%d[%d]", host, port, token);
    }
}
