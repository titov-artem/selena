package ru.selena.core.exception;

/**
 * Date: 2/28/13
 * Time: 1:47 AM
 *
 * @author Artem Titov
 */
public class ClusterException extends Exception {
    public ClusterException(final String message) {
        super(message);
    }

    public ClusterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
