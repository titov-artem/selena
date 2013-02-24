package ru.selena.core.impl;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.RemoteStoreService;
import ru.selena.core.exception.DataStoreException;
import ru.selena.core.exception.UpdatingOlderVersionException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.TransportService;
import ru.selena.net.exception.ConflictException;
import ru.selena.net.exception.NoDataException;
import ru.selena.net.model.Host;
import ru.selena.net.utils.SerializationUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Date: 12/18/12
 * Time: 11:24 PM
 *
 * @author Artem Titov
 */
public class RemoteStoreServiceImpl implements RemoteStoreService {

    private TransportService transportService;

    @Required
    public void setTransportService(final TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public DataObject get(final Key key, final Host remoteHost) throws DataStoreException {
        Validate.notNull(key, "Key can't be null");
        Validate.notNull(remoteHost, "Remote host can't be null");

        final byte[] dataObject;
        try {
            dataObject = transportService.get(key.getValue(), remoteHost);
        } catch (NoDataException e) {
            throw new NoSuchElementException(String.format(
                    "Host: %s. %s", remoteHost, e.getMessage()));
        } catch (IOException e) {
            throw new DataStoreException(String.format(
                    "Failed to load data from remote host (key: %s, host: %s). Cause: %s",
                    Arrays.toString(key.getHash()), remoteHost, e.getMessage()), e);
        }
        try {
            return SerializationUtils.deserializeDataObject(dataObject, 0);
        } catch (RuntimeException e) {
            throw new DataStoreException(String.format(
                    "Failed to deserialize response from remote host (key: %s, host: %s)",
                    Arrays.toString(key.getHash()), remoteHost), e);
        }
    }

    @Override
    public void put(final DataObject dataObject, final Host remoteHost) throws DataStoreException {
        Validate.notNull(dataObject, "Data object can't be null");
        Validate.notNull(remoteHost, "Remote host can't be null");

        final byte[] buffer = new byte[SerializationUtils.getRequiredSize(dataObject)];
        SerializationUtils.serializeDataObject(dataObject, buffer, 0);
        try {
            transportService.send(buffer, remoteHost);
        } catch (ConflictException e) {
            throw new UpdatingOlderVersionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new DataStoreException(String.format(
                    "Failed to send data to remote host (key: %s, host: %s). Cause: %s",
                    Arrays.toString(dataObject.getKey().getHash()), remoteHost, e.getMessage()), e);
        }
    }
}
