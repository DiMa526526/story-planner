package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.EventRequest;
import com.diana.storyplanner.dto.response.EventResponse;
import com.diana.storyplanner.service.EventService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления событиями в истории.
 * События являются основными строительными блоками сюжета.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    /**
     * Создание нового события в указанной истории.
     */
    @PostMapping("/stories/{storyId}/events")
    public ResponseEntity<EventResponse> createEvent(
            @PathVariable Long storyId,
            @Valid @RequestBody EventRequest request
    ) {
        log.debug("Создание события в истории {}: {}", storyId, request.getTitle());
        EventResponse response = eventService.createEvent(storyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех событий указанной истории.
     */
    @GetMapping("/stories/{storyId}/events")
    public ResponseEntity<List<EventResponse>> getEventsByStory(@PathVariable Long storyId) {
        log.debug("Получение всех событий истории: {}", storyId);
        List<EventResponse> events = eventService.getEventsByStory(storyId);
        return ResponseEntity.ok(events);
    }

    /**
     * Получение события по ID (без указания storyId).
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId) {
        log.debug("Получение события по ID: {}", eventId);
        EventResponse event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Обновление данных события.
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest request
    ) {
        log.debug("Обновление события с ID: {}", eventId);
        EventResponse response = eventService.updateEvent(eventId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление события.
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        log.debug("Удаление события с ID: {}", eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}