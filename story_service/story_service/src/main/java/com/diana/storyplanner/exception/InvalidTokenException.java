package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке использовать недействительный или просроченный JWT токен.
 * Возникает при восстановлении пароля или при неверном токене аутентификации.
 * HTTP статус: 400 Bad Request.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}