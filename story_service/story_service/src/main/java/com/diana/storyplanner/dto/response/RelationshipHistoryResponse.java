package com.diana.storyplanner.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Ответ с данными о записи в истории отношений.
 * Возвращается при запросе истории изменений отношений между персонажами.
 */
@Data
@Builder
public class RelationshipHistoryResponse {

    private Long id;

    private Long relationshipId;

    private Long eventId;

    private String eventTitle;

    private String relationshipType;

    private String color;
}