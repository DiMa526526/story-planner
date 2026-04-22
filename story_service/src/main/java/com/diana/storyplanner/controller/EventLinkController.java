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
 * Контроллер для управления связями между событиями (граф сюжета).
 * Позволяет создавать нелинейные ветвления в истории.
 */
@RestController
@RequestMapping("/stories/{storyId}/events/{eventId}/links")
@RequiredArgsConstructor
@Slf4j
public class EventLinkController {

    private final EventLinkService eventLinkService;

    /**
     * Создание связи от одного события к другому.
     * Определяет, какой выбор пользователя ведёт к какому событию.
     */
    @PostMapping
    public ResponseEntity<EventLinkResponse> createEventLink(
            @PathVariable Long eventId,
            @Valid @RequestBody EventLinkRequest request
    ) {
        log.debug("Создание связи от события {} к событию {}", eventId, request.getToEventId());
        EventLinkResponse response = eventLinkService.createEventLink(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех исходящих связей события.
     * Показывает, в какие события можно перейти из данного.
     */
    @GetMapping("/outgoing")
    public ResponseEntity<List<EventLinkResponse>> getOutgoingLinks(@PathVariable Long eventId) {
        log.debug("Получение исходящих связей события: {}", eventId);
        List<EventLinkResponse> links = eventLinkService.getOutgoingLinks(eventId);
        return ResponseEntity.ok(links);
    }

    /**
     * Получение всех входящих связей события.
     * Показывает, из каких событий можно прийти в данное.
     */
    @GetMapping("/incoming")
    public ResponseEntity<List<EventLinkResponse>> getIncomingLinks(@PathVariable Long eventId) {
        log.debug("Получение входящих связей события: {}", eventId);
        List<EventLinkResponse> links = eventLinkService.getIncomingLinks(eventId);
        return ResponseEntity.ok(links);
    }

    /**
     * Обновление существующей связи между событиями.
     */
    @PutMapping("/{linkId}")
    public ResponseEntity<EventLinkResponse> updateEventLink(
            @PathVariable Long linkId,
            @Valid @RequestBody EventLinkRequest request
    ) {
        log.debug("Обновление связи событий с ID: {}", linkId);
        EventLinkResponse response = eventLinkService.updateEventLink(linkId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление связи между событиями.
     */
    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> deleteEventLink(@PathVariable Long linkId) {
        log.debug("Удаление связи событий с ID: {}", linkId);
        eventLinkService.deleteEventLink(linkId);
        return ResponseEntity.noContent().build();
    }
}