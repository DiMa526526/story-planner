package com.diana.storyplanner.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ответ с данными о событии.
 * Возвращается при запросе информации о событии в истории.
 */
@Data
@Builder
public class EventResponse {

    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<CharacterResponse> characters;

    private List<EventLinkResponse> outgoingLinks;
}