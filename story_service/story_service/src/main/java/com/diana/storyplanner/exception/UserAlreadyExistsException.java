package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке регистрации пользователя
 * с уже существующим email или username.
 * HTTP статус: 400 Bad Request.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}