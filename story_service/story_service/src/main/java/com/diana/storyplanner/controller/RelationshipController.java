package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.RelationshipRequest;
import com.diana.storyplanner.dto.response.RelationshipResponse;
import com.diana.storyplanner.service.RelationshipService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления связями между персонажами.
 * Определяет, какие персонажи имеют отношения друг с другом.
 */
@RestController
@RequestMapping("/stories/{storyId}/relationships")
@RequiredArgsConstructor
@Slf4j
public class RelationshipController {

    private final RelationshipService relationshipService;

    /**
     * Создание связи между двумя персонажами.
     */
    @PostMapping
    public ResponseEntity<RelationshipResponse> createRelationship(
            @PathVariable Long storyId,
            @Valid @RequestBody RelationshipRequest request
    ) {
        log.debug("Создание связи между персонажами в истории {}", storyId);
        RelationshipResponse response = relationshipService.createRelationship(storyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Обновление существующей связи между персонажами.
     */
    @PutMapping("/{relationshipId}")
    public ResponseEntity<RelationshipResponse> updateRelationship(
            @PathVariable Long relationshipId,
            @Valid @RequestBody RelationshipRequest request
    ) {
        log.debug("Обновление связи персонажей с ID: {}", relationshipId);
        RelationshipResponse response = relationshipService.updateRelationship(relationshipId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение всех связей между персонажами в указанной истории.
     */
    @GetMapping
    public ResponseEntity<List<RelationshipResponse>> getRelationshipsByStory(@PathVariable Long storyId) {
        log.debug("Получение всех связей истории: {}", storyId);
        List<RelationshipResponse> relationships = relationshipService.getRelationshipsByStory(storyId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * Получение связи по ID.
     */
    @GetMapping("/{relationshipId}")
    public ResponseEntity<RelationshipResponse> getRelationshipById(@PathVariable Long relationshipId) {
        log.debug("Получение связи по ID: {}", relationshipId);
        RelationshipResponse relationship = relationshipService.getRelationshipById(relationshipId);
        return ResponseEntity.ok(relationship);
    }

    /**
     * Удаление связи между персонажами.
     */
    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> deleteRelationship(@PathVariable Long relationshipId) {
        log.debug("Удаление связи с ID: {}", relationshipId);
        relationshipService.deleteRelationship(relationshipId);
        return ResponseEntity.noContent().build();
    }
}