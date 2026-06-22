package com.diana.storyplanner.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для стандартизированного ответа с ошибкой.
 * Используется в GlobalExceptionHandler для единообразного формата ошибок API.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;

    private String message;

    private LocalDateTime timestamp;
}