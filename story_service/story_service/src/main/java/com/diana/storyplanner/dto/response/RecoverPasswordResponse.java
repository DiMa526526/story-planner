package com.diana.storyplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ответ при восстановлении пароля.
 * Содержит сообщение о результате операции.
 */
@Data
@AllArgsConstructor
public class RecoverPasswordResponse {

    private String message;
}