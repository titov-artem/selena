package ru.selena.core.exception;

/**
 * Date: 2/28/13
 * Time: 1:48 AM
 *
 * @author Artem Titov
 */
public class ClusterConnectionException extends ClusterException {
    public ClusterConnectionException(final String message) {
        super(message);
    }

    public ClusterConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
