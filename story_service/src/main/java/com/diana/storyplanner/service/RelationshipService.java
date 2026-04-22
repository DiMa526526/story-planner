package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.RelationshipRequest;
import com.diana.storyplanner.dto.response.RelationshipResponse;
import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.entity.CharacterRelationship;
import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.exception.SelfLinkException;
import com.diana.storyplanner.repository.CharacterRelationshipRepository;
import com.diana.storyplanner.repository.CharacterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления связями между персонажами.
 * Определяет, какие персонажи имеют отношения друг с другом в истории.
 * История изменений отношений хранится в RelationshipHistory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipService {

    private final CharacterRelationshipRepository relationshipRepository;
    private final CharacterRepository characterRepository;
    private final StoryService storyService;

    /**
     * Поиск связи между персонажами по ID с проверкой прав доступа.
     */
    public CharacterRelationship findRelationshipByIdAndUser(Long id) {
        return relationshipRepository.findById(id)
                .filter(rel -> rel.getStory().getUser() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Связь не найдена или доступ запрещен"));
    }

    /**
     * Создание связи между двумя персонажами в указанной истории.
     * Проверяет, что оба персонажа принадлежат этой истории.
     */
    @Transactional
    public RelationshipResponse createRelationship(Long storyId, RelationshipRequest request) {
        Story story = storyService.findStoryByIdAndUser(storyId);

        Character character1 = characterRepository.findById(request.getCharacter1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Персонаж 1 не найден"));
        Character character2 = characterRepository.findById(request.getCharacter2Id())
                .orElseThrow(() -> new ResourceNotFoundException("Персонаж 2 не найден"));

        // Проверка принадлежности персонажей к этой истории
        if (!character1.getStory().getId().equals(storyId) || !character2.getStory().getId().equals(storyId)) {
            throw new IllegalArgumentException("Персонажи должны принадлежать этой истории");
        }

        CharacterRelationship relationship = CharacterRelationship.builder()
                .story(story)
                .character1(character1)
                .character2(character2)
                .build();

        relationship = relationshipRepository.save(relationship);
        log.info("Создана связь между персонажами: {} и {}", character1.getName(), character2.getName());

        return mapToResponse(relationship);
    }

    /**
     * Обновление существующей связи между персонажами.
     * Позволяет изменить пару персонажей в отношениях.
     */
    @Transactional
    public RelationshipResponse updateRelationship(Long id, RelationshipRequest request) {
        CharacterRelationship relationship = findRelationshipByIdAndUser(id);

        Character character1 = characterRepository.findById(request.getCharacter1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Персонаж 1 не найден"));
        Character character2 = characterRepository.findById(request.getCharacter2Id())
                .orElseThrow(() -> new ResourceNotFoundException("Персонаж 2 не найден"));

        // Проверка принадлежности персонажей к этой истории
        if (!character1.getStory().getId().equals(relationship.getStory().getId()) ||
                !character2.getStory().getId().equals(relationship.getStory().getId())) {
            throw new IllegalArgumentException("Персонажи должны принадлежать этой истории");
        }

        // Запрет на связь персонажа с самим собой
        if (request.getCharacter1Id().equals(request.getCharacter2Id())) {
            throw new SelfLinkException("Нельзя создать связь персонажа с самим собой");
        }

        relationship.setCharacter1(character1);
        relationship.setCharacter2(character2);

        relationship = relationshipRepository.save(relationship);
        log.info("Обновлена связь между персонажами: id={}", id);

        return mapToResponse(relationship);
    }

    /**
     * Получение всех связей между персонажами в указанной истории.
     */
    public List<RelationshipResponse> getRelationshipsByStory(Long storyId) {
        storyService.findStoryByIdAndUser(storyId);
        return relationshipRepository.findByStoryId(storyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение связи по ID с проверкой доступа.
     */
    public RelationshipResponse getRelationshipById(Long id) {
        CharacterRelationship relationship = findRelationshipByIdAndUser(id);
        return mapToResponse(relationship);
    }

    /**
     * Удаление связи между персонажами.
     * Вместе со связью удаляется вся история отношений (RelationshipHistory).
     */
    @Transactional
    public void deleteRelationship(Long id) {
        CharacterRelationship relationship = findRelationshipByIdAndUser(id);
        relationshipRepository.delete(relationship);
        log.info("Удалена связь между персонажами: id={}", id);
    }

    /**
     * Преобразование сущности CharacterRelationship в RelationshipResponse DTO.
     */
    private RelationshipResponse mapToResponse(CharacterRelationship relationship) {
        return RelationshipResponse.builder()
                .id(relationship.getId())
                .character1(mapToCharacterResponse(relationship.getCharacter1()))
                .character2(mapToCharacterResponse(relationship.getCharacter2()))
                .build();
    }

    /**
     * Преобразование сущности Character в CharacterResponse DTO.
     */
    private CharacterResponse mapToCharacterResponse(Character character) {
        return CharacterResponse.builder()
                .id(character.getId())
                .name(character.getName())
                .description(character.getDescription())
                .imageUrl(character.getImageUrl())
                .storyId(character.getStory().getId())
                .build();
    }
}