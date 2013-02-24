package ru.selena.core.exception;

/**
 * Date: 12/17/12
 * Time: 1:19 AM
 *
 * @author Artem Titov
 */
public class DataStoreException extends Exception {
    public DataStoreException(final String message) {
        super(message);
    }

    public DataStoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
