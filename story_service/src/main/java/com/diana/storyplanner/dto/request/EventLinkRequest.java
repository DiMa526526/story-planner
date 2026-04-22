package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на создание или обновление связи между событиями.
 * Определяет переходы в нелинейном сюжете.
 */
@Data
public class EventLinkRequest {

    /**
     * ID целевого события (куда ведёт переход).
     * Обязательное поле.
     */
    @NotNull(message = "ID целевого события обязательно")
    private Long toEventId;

    private String choiceText;
}