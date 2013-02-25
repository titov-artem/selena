package ru.selena.model;

import ru.selena.model.Key;

/**
 * Date: 12/18/12
 * Time: 11:43 PM
 *
 * @author Artem Titov
 */
public interface KeyFactory {

    /**
     * Create key object by it's byte representation.
     *
     * @param key key byte representation
     * @return key object
     */
    Key createKey(final byte[] key);
}
