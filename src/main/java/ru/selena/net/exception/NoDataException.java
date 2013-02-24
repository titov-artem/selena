package ru.selena.net.exception;

import java.io.IOException;

/**
 * Date: 12/24/12
 * Time: 1:45 AM
 *
 * @author Artem Titov
 */
public class NoDataException extends IOException {
    public NoDataException(final String message) {
        super(message);
    }
}
