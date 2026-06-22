package com.diana.storyplanner.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Ответ с данными о связи между событиями.
 * Возвращается при запросе информации о переходе в сюжете.
 */
@Data
@Builder
public class EventLinkResponse {

    private Long id;

    private Long fromEventId;

    private Long toEventId;

    private String choiceText;

    private String toEventTitle;
}