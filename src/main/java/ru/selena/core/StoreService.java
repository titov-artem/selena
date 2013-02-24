package ru.selena.core;

import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;

/**
 * Date: 12/17/12
 * Time: 9:45 PM
 *
 * @author Artem Titov
 */
public interface StoreService {

    /**
     * Retrieve data from local node by the key.
     *
     * @param key key, not null
     * @return <ul>
     *         <li>Data object associate with this key</li>
     *         <li>Special data object with {@link ru.selena.model.DataObject#isStub()} {@code true} if key was deleted</li>
     *         </ul>
     * @throws java.util.NoSuchElementException
     *                              if no any element associated with this key found
     * @throws DataStoreException   if operation fails
     * @throws NullPointerException if key is null
     */
    DataObject get(final Key key) throws DataStoreException;

    /**
     * Store data on the local node by the key.
     *
     * @param dataObject data object to store, not null
     * @throws DataStoreException   if operation fails
     * @throws NullPointerException if dataObject is null
     */
    void put(final DataObject dataObject) throws DataStoreException;
}
