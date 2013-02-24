package ru.selena.net.exception;

import java.io.IOException;

/**
 * Date: 12/24/12
 * Time: 1:19 AM
 *
 * @author Artem Titov
 */
public class ConflictException extends IOException {
    public ConflictException(final String message) {
        super(message);
    }
}
