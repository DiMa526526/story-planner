package com.diana.storyplanner.dto.request;

import com.diana.storyplanner.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на создание или обновление истории.
 * Содержит основную информацию о сюжете.
 */
@Data
public class StoryRequest {

    @NotBlank(message = "Название обязательно")
    private String title;

    private String shortDescription;

    private String fullDescription;

    private Genre genre;

    private String coverUrl;
}