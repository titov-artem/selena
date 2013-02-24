package ru.selena.net.model;

/**
 * Date: 12/15/12
 * Time: 4:06 PM
 *
 * @author Artem Titov
 */
public interface Host {


    /**
     * Return remote host name.
     *
     * @return host name
     */
    String getHost();

    /**
     * Return remote port.
     *
     * @return port
     */
    int getPort();

    /**
     * Return host token.
     *
     * @return not null not empty byte array with host token
     */
    byte[] getToken();

}
