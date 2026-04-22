package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * Содержит все необходимые данные для создания аккаунта.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}