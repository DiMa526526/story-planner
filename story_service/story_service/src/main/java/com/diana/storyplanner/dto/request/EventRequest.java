package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * DTO для запроса на создание или обновление события.
 * Содержит информацию о событии и список участвующих персонажей.
 */
@Data
public class EventRequest {

    /**
     * Название события.
     * Обязательное поле.
     */
    @NotBlank(message = "Название события обязательно")
    private String title;

    private String content;

    private List<Long> characterIds;
}