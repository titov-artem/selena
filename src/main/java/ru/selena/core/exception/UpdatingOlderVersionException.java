package ru.selena.core.exception;

/**
 * Throws when somebody is trying to update existing existing data object with it's older version.
 * <p/>
 * Date: 12/19/12
 * Time: 3:41 PM
 *
 * @author Artem Titov
 */
public class UpdatingOlderVersionException extends DataStoreException {

    public UpdatingOlderVersionException(final String message) {
        super(message);
    }

    public UpdatingOlderVersionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
