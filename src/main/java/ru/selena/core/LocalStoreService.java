package ru.selena.core;

import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;

import java.io.IOException;

/**
 * Date: 12/16/12
 * Time: 1:40 AM
 *
 * @author Artem Titov
 */
public interface LocalStoreService extends StoreService {

    /**
     * Open storage.
     *
     * @return storage status, which determine available operations
     * @throws IOException if failed to open storage
     */
    StorageStatus open() throws IOException;

    /**
     * Restore storage to consistent state.
     *
     * @return status of the storage after restore
     * @throws IOException if restore failed
     */
    StorageStatus restore() throws IOException;

    /**
     * @inheritDoc
     */
    @Override
    DataObject get(final Key key) throws DataStoreException;

    /**
     * @throws ru.selena.core.exception.UpdatingOlderVersionException
     *          if data object version older than existing
     *          data object version
     * @inheritDoc
     */
    @Override
    void put(final DataObject dataObject) throws DataStoreException;

    /**
     * Close storage.
     *
     * @throws IOException if failed to close storage
     */
    void close() throws IOException;

    enum StorageStatus {
        OK, CORRUPTED
    }
}
