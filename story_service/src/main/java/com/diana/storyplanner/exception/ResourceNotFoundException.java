package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему ресурсу.
 * Например, когда история, персонаж или событие не найдены в базе данных.
 * HTTP статус: 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}