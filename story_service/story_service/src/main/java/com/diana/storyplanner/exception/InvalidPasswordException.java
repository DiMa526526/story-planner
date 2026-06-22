package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке входа с неверным паролем.
 * HTTP статус: 401 Unauthorized.
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}