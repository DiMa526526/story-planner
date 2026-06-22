package com.diana.storyplanner.dto.response;

import com.diana.storyplanner.entity.Genre;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Ответ с данными о истории.
 * Возвращается при запросе информации об истории пользователя.
 */
@Data
@Builder
public class StoryResponse {

    private Long id;

    private String title;

    private String shortDescription;

    private String fullDescription;

    private Genre genre;

    private String coverUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer charactersCount;

    private Integer eventsCount;
}