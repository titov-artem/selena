package ru.selena.core;

import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;

/**
 * Date: 12/16/12
 * Time: 1:40 AM
 *
 * @author Artem Titov
 */
public interface LocalStoreService extends StoreService {

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
}
