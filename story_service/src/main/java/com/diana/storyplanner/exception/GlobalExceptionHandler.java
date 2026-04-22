package com.diana.storyplanner.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Перехватывает все исключения, выбрасываемые в контроллерах,
 * и преобразует их в стандартизированный формат ErrorResponse.
 * <p>
 * Обеспечивает единообразный формат ошибок API и правильные HTTP статусы.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Вспомогательный метод для построения стандартного ответа с ошибкой.
     */
    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Обработка ошибки аутентификации (неавторизованный доступ).
     * Возникает, когда пользователь не предоставил JWT токен или токен недействителен.
     * HTTP статус: 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Неавторизованный доступ: {}", ex.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, "Вы не авторизованы. Пожалуйста, войдите в систему.");
    }

    /**
     * Обработка ошибки доступа (запрещено).
     * Возникает, когда у пользователя нет прав для выполнения действия.
     * HTTP статус: 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Доступ запрещён: {}", ex.getMessage());
        return buildError(HttpStatus.FORBIDDEN, "У вас нет прав для выполнения этого действия.");
    }

    /**
     * Обработка ошибки "Ресурс не найден".
     * Возникает, когда запрашиваемый ресурс отсутствует в базе данных.
     * HTTP статус: 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Обработка ошибки "Пользователь не найден".
     * HTTP статус: 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Пользователь не найден: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Обработка ошибки "Пользователь уже существует".
     * Возникает при регистрации с уже занятым email или username.
     * HTTP статус: 400 Bad Request.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Обработка ошибки "Неверный пароль".
     * HTTP статус: 401 Unauthorized.
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Неверный пароль: {}", ex.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Обработка ошибки "Неверный токен".
     * Возникает при восстановлении пароля с просроченным или недействительным токеном.
     * HTTP статус: 400 Bad Request.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Неверный токен: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Обработка ошибки "Недопустимый аргумент".
     * Возникает при нарушении бизнес-логики (например, персонажи из разных историй).
     * HTTP статус: 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Обработка ошибки "Дублирование связи".
     * Возникает при попытке создать уже существующую связь между событиями.
     * HTTP статус: 409 Conflict.
     */
    @ExceptionHandler(DuplicateRelationshipException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRelationship(DuplicateRelationshipException ex) {
        log.warn("Дублирование: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Обработка ошибки "Связь с самим собой".
     * Возникает при попытке создать связь события или персонажа с самим собой.
     * HTTP статус: 400 Bad Request.
     */
    @ExceptionHandler(SelfLinkException.class)
    public ResponseEntity<ErrorResponse> handleSelfLink(SelfLinkException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Обработка ошибок валидации @Valid аннотаций.
     * Возникает, когда входящие DTO не проходят валидацию.
     * HTTP статус: 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Ошибка валидации: {}", errors);
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации: " + errors);
    }

    /**
     * Обработка всех остальных необработанных исключений.
     * Возникает при непредвиденных ошибках на сервере.
     * HTTP статус: 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Неизвестная ошибка", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера: " + ex.getMessage());
    }
}