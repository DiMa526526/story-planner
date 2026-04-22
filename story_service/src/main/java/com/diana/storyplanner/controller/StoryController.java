package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.StoryRequest;
import com.diana.storyplanner.dto.response.StoryResponse;
import com.diana.storyplanner.service.StoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления историями.
 * Предоставляет CRUD операции для историй пользователя.
 * Все эндпоинты требуют JWT аутентификации.
 */
@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
@Slf4j
public class StoryController {

    private final StoryService storyService;

    /**
     * Создание новой истории.
     * История привязывается к текущему авторизованному пользователю.
     */
    @PostMapping
    public ResponseEntity<StoryResponse> createStory(@Valid @RequestBody StoryRequest request) {
        log.debug("Создание новой истории: {}", request.getTitle());
        StoryResponse response = storyService.createStory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех историй текущего пользователя.
     */
    @GetMapping
    public ResponseEntity<List<StoryResponse>> getUserStories() {
        log.debug("Получение всех историй пользователя");
        List<StoryResponse> stories = storyService.getUserStories();
        return ResponseEntity.ok(stories);
    }

    /**
     * Получение истории по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> getStoryById(@PathVariable Long id) {
        log.debug("Получение истории по ID: {}", id);
        StoryResponse story = storyService.getStoryById(id);
        return ResponseEntity.ok(story);
    }

    /**
     * Обновление существующей истории.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoryResponse> updateStory(
            @PathVariable Long id,
            @Valid @RequestBody StoryRequest request
    ) {
        log.debug("Обновление истории с ID: {}", id);
        StoryResponse response = storyService.updateStory(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление истории.
     * При удалении истории каскадно удаляются все связанные данные:
     * персонажи, события, связи.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        log.debug("Удаление истории с ID: {}", id);
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }
}