package ru.selena.model;

/**
 *
 * Date: 12/15/12
 * Time: 3:57 PM
 *
 * @author Artem Titov
 */
public interface Key {

    /**
     * Return value of the key.
     *
     * @return not null and not empty byte array with value
     */
    byte[] getValue();

    /**
     * Return a hash for key. This hash will use in key ring to distribute key among db nodes
     *
     * @return hash
     */
    byte[] getHash();

}
