package ru.selena.model.factory;

import ru.selena.model.Version;

/**
 * Date: 12/18/12
 * Time: 11:44 PM
 *
 * @author Artem Titov
 */
public interface VersionFactory {

    /**
     * Create version object by it's byte representation.
     *
     * @param version version byte representation
     * @return version object
     */
    Version createVersion(final byte[] version);
}
