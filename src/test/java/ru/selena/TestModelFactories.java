package ru.selena;

import ru.selena.model.*;
import ru.selena.model.impl.*;

/**
 * Date: 2/25/13
 * Time: 11:07 PM
 *
 * @author Artem Titov
 */
public final class TestModelFactories {

    private static final DataObjectFactory dataObjectFactory = new DataObjectFactoryImpl();
    private static final KeyFactory keyFactory = new IntegerHashKeyFactory();
    private static final VersionFactory versionFactory = new LongVersionFactory();

    public static DataObject createDataObject(final Key key, final Version version, final byte[] value) {
        return dataObjectFactory.createDataObject(key, version, value);
    }

    public static DataObject createDataObject(final Key key, final Version version) {
        return dataObjectFactory.createDataObject(key, version);
    }

    public static Key createKey(final byte[] key) {
        return keyFactory.createKey(key);
    }

    public static Version createVersion(final byte[] version) {
        return versionFactory.createVersion(version);
    }
}
