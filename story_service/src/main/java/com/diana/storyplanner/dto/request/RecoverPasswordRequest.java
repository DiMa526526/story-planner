package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на восстановление пароля по токену.
 * Пользователь получает токен по email и отправляет его вместе с новым паролем.
 */
@Data
public class RecoverPasswordRequest {

    @NotBlank(message = "Токен обязателен")
    private String token;

    @NotBlank(message = "Новый пароль обязателен")
    private String newPassword;
}