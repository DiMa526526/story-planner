package com.diana.storyplanner.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Ответ с данными о связи между персонажами.
 * Возвращается при запросе информации об отношениях персонажей.
 */
@Data
@Builder
public class RelationshipResponse {

    private Long id;

    private CharacterResponse character1;

    private CharacterResponse character2;
}