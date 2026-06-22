package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на проверку кода подтверждения.
 */
@Data
public class VerifyCodeRequest {

    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Код подтверждения обязателен")
    private String code;
}