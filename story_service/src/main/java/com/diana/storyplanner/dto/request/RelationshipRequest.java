package com.diana.storyplanner.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на создание или обновление связи между персонажами.
 * Определяет, какие два персонажа имеют отношения друг с другом.
 */
@Data
public class RelationshipRequest {

    @NotNull(message = "ID первого персонажа обязательно")
    private Long character1Id;

    @NotNull(message = "ID второго персонажа обязательно")
    private Long character2Id;
}