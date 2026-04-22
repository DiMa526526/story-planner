package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.CharacterRequest;
import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.CharacterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления персонажами историй.
 * Предоставляет CRUD операции для персонажей,
 * а также проверку прав доступа через StoryService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final StoryService storyService;

    /**
     * Создание нового персонажа в указанной истории.
     * Сначала проверяет доступ пользователя к истории,
     * затем создаёт и сохраняет персонажа.
     */
    @Transactional
    public CharacterResponse createCharacter(Long storyId, CharacterRequest request) {
        // Проверка доступа к истории
        Story story = storyService.findStoryByIdAndUser(storyId);

        // Создание нового персонажа
        Character character = Character.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .story(story)
                .build();

        character = characterRepository.save(character);
        log.info("Создан персонаж: id={}, name={}, storyId={}", character.getId(), character.getName(), storyId);

        return mapToResponse(character);
    }

    /**
     * Получение всех персонажей указанной истории.
     */
    public List<CharacterResponse> getCharactersByStory(Long storyId) {
        // Проверка доступа к истории
        storyService.findStoryByIdAndUser(storyId);

        return characterRepository.findByStoryId(storyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение персонажа по ID с проверкой доступа.
     */
    public CharacterResponse getCharacterById(Long id) {
        Character character = findCharacterByIdAndUser(id);
        return mapToResponse(character);
    }

    /**
     * Обновление данных персонажа.
     */
    @Transactional
    public CharacterResponse updateCharacter(Long id, CharacterRequest request) {
        Character character = findCharacterByIdAndUser(id);

        character.setName(request.getName());
        character.setDescription(request.getDescription());
        character.setImageUrl(request.getImageUrl());

        character = characterRepository.save(character);
        log.info("Обновлен персонаж: id={}", id);

        return mapToResponse(character);
    }

    /**
     * Удаление персонажа.
     * Вместе с персонажем удаляются все его связи (события, отношения).
     */
    @Transactional
    public void deleteCharacter(Long id) {
        Character character = findCharacterByIdAndUser(id);
        characterRepository.delete(character);
        log.info("Удален персонаж: id={}", id);
    }

    /**
     * Вспомогательный метод для поиска персонажа с проверкой прав.
     * Проверяет, что персонаж принадлежит истории текущего пользователя.
     */
    private Character findCharacterByIdAndUser(Long id) {
        return characterRepository.findById(id)
                .filter(character -> character.getStory().getUser() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Персонаж не найден или доступ запрещен"));
    }

    /**
     * Преобразование сущности Character в CharacterResponse DTO.
     */
    private CharacterResponse mapToResponse(Character character) {
        return CharacterResponse.builder()
                .id(character.getId())
                .name(character.getName())
                .description(character.getDescription())
                .imageUrl(character.getImageUrl())
                .storyId(character.getStory().getId())
                .build();
    }
}