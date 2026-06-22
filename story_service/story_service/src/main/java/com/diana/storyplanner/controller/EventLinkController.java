package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.EventLinkRequest;
import com.diana.storyplanner.dto.response.EventLinkResponse;
import com.diana.storyplanner.service.EventLinkService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления связями между событиями.
 * Позволяет строить нелинейные сюжеты с ветвлениями.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EventLinkController {

    private final EventLinkService eventLinkService;

    /**
     * Создание связи между двумя событиями.
     * Определяет, из какого события можно перейти в какое.
     */
    @PostMapping("/events/{fromEventId}/links")
    public ResponseEntity<EventLinkResponse> createLink(
            @PathVariable Long fromEventId,
            @Valid @RequestBody EventLinkRequest request
    ) {
        log.debug("Создание связи из события {} в событие {}", fromEventId, request.getToEventId());
        EventLinkResponse response = eventLinkService.createLink(fromEventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Обновление существующей связи между событиями.
     * Позволяет изменить целевое событие и текст выбора.
     */
    @PutMapping("/events/links/{linkId}")
    public ResponseEntity<EventLinkResponse> updateLink(
            @PathVariable Long linkId,
            @Valid @RequestBody EventLinkRequest request
    ) {
        log.debug("Обновление связи событий с ID: {}", linkId);
        EventLinkResponse response = eventLinkService.updateLink(linkId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение всех исходящих связей из указанного события.
     */
    @GetMapping("/events/{eventId}/links/outgoing")
    public ResponseEntity<List<EventLinkResponse>> getOutgoingLinks(@PathVariable Long eventId) {
        log.debug("Получение исходящих связей события: {}", eventId);
        List<EventLinkResponse> links = eventLinkService.getOutgoingLinks(eventId);
        return ResponseEntity.ok(links);
    }

    /**
     * Получение всех входящих связей к указанному событию.
     */
    @GetMapping("/events/{eventId}/links/incoming")
    public ResponseEntity<List<EventLinkResponse>> getIncomingLinks(@PathVariable Long eventId) {
        log.debug("Получение входящих связей события: {}", eventId);
        List<EventLinkResponse> links = eventLinkService.getIncomingLinks(eventId);
        return ResponseEntity.ok(links);
    }

    /**
     * Удаление связи между событиями.
     */
    @DeleteMapping("/events/links/{linkId}")
    public ResponseEntity<Void> deleteLink(@PathVariable Long linkId) {
        log.debug("Удаление связи событий с ID: {}", linkId);
        eventLinkService.deleteLink(linkId);
        return ResponseEntity.noContent().build();
    }
}