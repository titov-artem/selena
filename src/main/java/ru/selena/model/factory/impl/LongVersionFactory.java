package ru.selena.model.factory.impl;

import ru.selena.model.Version;
import ru.selena.model.factory.VersionFactory;
import ru.selena.model.impl.LongVersion;

/**
 * Date: 12/18/12
 * Time: 11:58 PM
 *
 * @author Artem Titov
 */
public class LongVersionFactory implements VersionFactory {

    @Override
    public Version createVersion(final byte[] version) {
        return new LongVersion(version);
    }
}
