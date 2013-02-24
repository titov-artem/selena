package ru.selena.model.factory.impl;

import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.model.Version;
import ru.selena.model.factory.DataObjectFactory;
import ru.selena.model.impl.DataObjectImpl;

/**
 * Date: 12/18/12
 * Time: 11:59 PM
 *
 * @author Artem Titov
 */
public class DataObjectFactoryImpl implements DataObjectFactory {

    @Override
    public DataObject createDataObject(final Key key, final Version version, final byte[] value) {
        return new DataObjectImpl(key, version, value);
    }

    @Override
    public DataObject createDataObject(final Key key, final Version version) {
        return new DataObjectImpl(key, version);
    }
}
