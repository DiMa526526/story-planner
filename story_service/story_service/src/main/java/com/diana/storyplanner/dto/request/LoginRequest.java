package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на аутентификацию (вход в систему).
 * Пользователь может ввести как email, так и username.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Введите email или username")
    private String usernameOrEmail;

    @NotBlank(message = "Введите пароль")
    private String password;
}