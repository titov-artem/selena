package ru.selena.core;

import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.model.Host;

/**
 * Date: 12/17/12
 * Time: 1:16 AM
 *
 * @author Artem Titov
 */
public interface RemoteStoreService {

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
     * @throws NullPointerException if any parameter is null
     */
    DataObject get(final Key key, final Host remoteHost) throws DataStoreException;

    /**
     * Store data on the remote node by the key.
     *
     * @param dataObject data object to store, not null
     * @param remoteHost remote node
     * @throws ru.selena.core.exception.UpdatingOlderVersionException
     *                              if data object version older than existing
     *                              data object version
     * @throws DataStoreException   if operation fails
     * @throws NullPointerException if any parameter is null
     */
    void put(final DataObject dataObject, final Host remoteHost) throws DataStoreException;

}
