package com.diana.storyplanner.dto.request;

import com.diana.storyplanner.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса на создание или обновление истории.
 * Содержит основную информацию о сюжете.
 */
@Data
public class StoryRequest {

    @NotBlank(message = "Название обязательно")
    @Size(max = 255, message = "Название не может превышать 255 символов")
    private String title;

    @Size(max = 500, message = "Краткое описание не может превышать 500 символов")
    private String shortDescription;

    @Size(max = 10000, message = "Полное описание не может превышать 10000 символов")
    private String fullDescription;

    private Genre genre;

    @Size(max = 2000, message = "URL обложки слишком длинный")
    private String coverUrl;
}