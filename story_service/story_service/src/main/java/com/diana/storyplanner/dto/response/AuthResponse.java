package com.diana.storyplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ответ при авторизации (регистрация или вход).
 * Содержит сообщение о результате и JWT токен для доступа к API.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String message;

    private String token;
}