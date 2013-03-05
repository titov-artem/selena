package ru.selena.core.exception;

/**
* Date: 3/3/13
* Time: 12:10 PM
*
* @author Artem Titov
*/
public class OperationFailedException extends RuntimeException {
    public OperationFailedException(final String message) {
        super(message);
    }

    public OperationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
