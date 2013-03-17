package ru.selena.core;

import java.util.List;

/**
 * Date: 3/3/13
 * Time: 12:27 PM
 *
 * @author Artem Titov
 */
public interface HostTokenGenerator {

    /**
     * Generate a new token by known tokens in the cluster.
     *
     * @param currentClusterTokens known tokens
     * @return new token
     */
    byte[] generateToken(final List<byte[]> currentClusterTokens);
}
