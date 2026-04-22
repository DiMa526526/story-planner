package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.dto.response.EventResponse;
import com.diana.storyplanner.service.EventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для получения персонажей, участвующих в событии.
 * Предоставляет удобный способ просмотра всех персонажей конкретного события.
 */
@RestController
@RequestMapping("/stories/{storyId}/events/{eventId}/characters")
@RequiredArgsConstructor
@Slf4j
public class EventCharacterController {

    private final EventService eventService;

    /**
     * Получение всех персонажей, участвующих в указанном событии.
     */
    @GetMapping
    public ResponseEntity<List<CharacterResponse>> getEventCharacters(@PathVariable Long eventId) {
        log.debug("Получение персонажей события: {}", eventId);
        EventResponse event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event.getCharacters());
    }
}