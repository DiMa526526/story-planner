package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.RelationshipHistoryRequest;
import com.diana.storyplanner.dto.response.RelationshipHistoryResponse;
import com.diana.storyplanner.entity.CharacterRelationship;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.RelationshipHistory;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.RelationshipHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления историей изменений отношений между персонажами.
 * Позволяет отслеживать, как менялись отношения персонажей
 * в процессе развития сюжета (после каждого события).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipHistoryService {

    private final RelationshipHistoryRepository historyRepository;
    private final RelationshipService relationshipService;
    private final EventService eventService;

    /**
     * Добавление записи в историю отношений.
     * Фиксирует, как изменились отношения между персонажами
     * после наступления определённого события.
     */
    @Transactional
    public RelationshipHistoryResponse addHistoryEntry(Long relationshipId, RelationshipHistoryRequest request) {
        // Проверка доступа к связи
        CharacterRelationship relationship = relationshipService.findRelationshipByIdAndUser(relationshipId);

        // Проверка доступа к событию
        Event event = eventService.findEventByIdAndUser(request.getEventId());

        // Проверка принадлежности события к той же истории
        if (!event.getStory().getId().equals(relationship.getStory().getId())) {
            throw new IllegalArgumentException("Событие должно принадлежать той же истории");
        }

        // Создание записи истории
        RelationshipHistory history = RelationshipHistory.builder()
                .relationship(relationship)
                .event(event)
                .relationshipType(request.getRelationshipType())
                .color(request.getColor())
                .build();

        history = historyRepository.save(history);
        log.info("Добавлена запись в историю отношений: relationshipId={}, type={}",
                relationshipId, request.getRelationshipType());

        return mapToResponse(history);
    }

    /**
     * Получение всей истории изменений для указанной связи.
     * Возвращает список записей в хронологическом порядке.
     */
    public List<RelationshipHistoryResponse> getHistoryByRelationship(Long relationshipId) {
        CharacterRelationship relationship = relationshipService.findRelationshipByIdAndUser(relationshipId);
        return historyRepository.findByRelationship(relationship)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Удаление записи из истории отношений.
     */
    @Transactional
    public void deleteHistoryEntry(Long historyId) {
        RelationshipHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись истории не найдена"));

        // Проверка доступа через связь
        relationshipService.findRelationshipByIdAndUser(history.getRelationship().getId());

        historyRepository.delete(history);
        log.info("Удалена запись истории: id={}", historyId);
    }

    /**
     * Преобразование сущности RelationshipHistory в RelationshipHistoryResponse DTO.
     */
    private RelationshipHistoryResponse mapToResponse(RelationshipHistory history) {
        return RelationshipHistoryResponse.builder()
                .id(history.getId())
                .relationshipId(history.getRelationship().getId())
                .eventId(history.getEvent().getId())
                .eventTitle(history.getEvent().getTitle())
                .relationshipType(history.getRelationshipType())
                .color(history.getColor())
                .build();
    }
}