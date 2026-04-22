package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке найти несуществующего пользователя.
 * HTTP статус: 404 Not Found.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}