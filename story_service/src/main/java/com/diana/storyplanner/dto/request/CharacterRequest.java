package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на создание или обновление персонажа.
 * Содержит основные данные о персонаже истории.
 */
@Data
public class CharacterRequest {

    /**
     * Имя персонажа.
     * Обязательное поле, не может быть пустым.
     */
    @NotBlank(message = "Имя персонажа обязательно")
    private String name;

    private String description;

    private String imageUrl;
}