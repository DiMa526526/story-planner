package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на добавление записи в историю отношений.
 * Фиксирует изменение отношений между персонажами после события.
 */
@Data
public class RelationshipHistoryRequest {

    @NotNull(message = "ID события обязательно")
    private Long eventId;

    @NotBlank(message = "Тип отношения обязателен")
    private String relationshipType;

    private String color;
}