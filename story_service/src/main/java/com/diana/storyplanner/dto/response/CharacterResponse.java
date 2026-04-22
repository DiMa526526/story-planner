package com.diana.storyplanner.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Ответ с данными персонажа.
 * Возвращается при запросе информации о персонаже.
 */
@Data
@Builder
public class CharacterResponse {

    private Long id;

    private String name;

    private String description;

    private String imageUrl;

    private Long storyId;
}