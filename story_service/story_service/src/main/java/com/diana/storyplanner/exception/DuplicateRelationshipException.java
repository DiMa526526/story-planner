package com.diana.storyplanner.exception;

/**
 * Исключение, выбрасываемое при попытке создать дублирующую связь.
 * Например, когда связь между событиями или персонажами уже существует.
 */
public class DuplicateRelationshipException extends RuntimeException {
    public DuplicateRelationshipException(String message) {
        super(message);
    }
}