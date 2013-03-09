package ru.selena.core.impl;

import ru.selena.core.LocalStoreService;
import ru.selena.core.exception.UpdatingOlderVersionException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.utils.collections.ArrayUtils;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Date: 12/17/12
 * Time: 12:34 AM
 *
 * @author Artem Titov
 */
public class InMemoryLocalStoreService implements LocalStoreService {

    private ConcurrentMap<Key, DataObject> storage = new ConcurrentHashMap<Key, DataObject>();

    @Override
    public StorageStatus open() throws IOException {
        return StorageStatus.OK;
    }

    @Override
    public StorageStatus restore() throws IOException {
        return StorageStatus.OK;
    }

    @Override
    public DataObject get(final Key key) {
        final DataObject dataObject = storage.get(key);
        if (dataObject == null) {
            throw new NoSuchElementException(ArrayUtils.toHexString(key.getHash()));
        }
        return dataObject;
    }

    @Override
    public void put(final DataObject dataObject) throws UpdatingOlderVersionException {
        final DataObject old = storage.get(dataObject.getKey());
        if (old != null && !dataObject.getVersion().isBefore(old.getVersion())) {
            throw new UpdatingOlderVersionException("Can't store older object");
        }
        storage.put(dataObject.getKey(), dataObject);
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
