package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке создать связь с самим собой.
 * Например, когда событие ссылается на само себя или персонаж связан с самим собой.
 * HTTP статус: 400 Bad Request.
 */
public class SelfLinkException extends RuntimeException {
    public SelfLinkException(String message) {
        super(message);
    }
}