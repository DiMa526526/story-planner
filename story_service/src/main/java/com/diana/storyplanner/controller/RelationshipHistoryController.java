package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.RelationshipHistoryRequest;
import com.diana.storyplanner.dto.response.RelationshipHistoryResponse;
import com.diana.storyplanner.service.RelationshipHistoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления историей изменений отношений между персонажами.
 * Позволяет отслеживать, как менялись отношения в процессе сюжета.
 */
@RestController
@RequestMapping("/stories/{storyId}/relationships/{relationshipId}/history")
@RequiredArgsConstructor
@Slf4j
public class RelationshipHistoryController {

    private final RelationshipHistoryService historyService;

    /**
     * Добавление записи в историю отношений.
     * Фиксирует изменение отношений после определённого события.
     */
    @PostMapping
    public ResponseEntity<RelationshipHistoryResponse> addHistoryEntry(
            @PathVariable Long relationshipId,
            @Valid @RequestBody RelationshipHistoryRequest request
    ) {
        log.debug("Добавление записи в историю отношений: {}", relationshipId);
        RelationshipHistoryResponse response = historyService.addHistoryEntry(relationshipId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всей истории изменений для указанной связи.
     * Возвращает список записей в хронологическом порядке.
     */
    @GetMapping
    public ResponseEntity<List<RelationshipHistoryResponse>> getHistoryByRelationship(
            @PathVariable Long relationshipId
    ) {
        log.debug("Получение истории отношений: {}", relationshipId);
        List<RelationshipHistoryResponse> history = historyService.getHistoryByRelationship(relationshipId);
        return ResponseEntity.ok(history);
    }

    /**
     * Удаление записи из истории отношений.
     */
    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> deleteHistoryEntry(@PathVariable Long historyId) {
        log.debug("Удаление записи истории с ID: {}", historyId);
        historyService.deleteHistoryEntry(historyId);
        return ResponseEntity.noContent().build();
    }
}