package ru.selena.net;

import ru.selena.net.exception.ConflictException;
import ru.selena.net.exception.NoDataException;
import ru.selena.net.model.Host;

import java.io.IOException;

/**
 * Date: 12/18/12
 * Time: 10:33 PM
 *
 * @author Artem Titov
 */
public interface TransportService {

    /**
     * Return data object bytes by key bytes.
     *
     * @param key  key bytes
     * @param host remote host
     * @return data object bytes
     * @throws NoDataException      if remote has no data associated with this key
     * @throws IOException          if communication with remote host failed
     * @throws NullPointerException if any parameter is null
     */
    byte[] get(final byte[] key, final Host host) throws IOException;

    /**
     * Send data object to remote host.
     *
     * @param dataObject data object
     * @param host       remote host
     * @throws ConflictException    if remote host respond about conflict
     * @throws IOException          if communication with remote host failed or remote host failed to process request
     * @throws NullPointerException if any parameter is null
     */
    void send(final byte[] dataObject, final Host host) throws IOException;
}
