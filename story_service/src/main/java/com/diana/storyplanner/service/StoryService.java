package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.StoryRequest;
import com.diana.storyplanner.dto.response.StoryResponse;
import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.entity.User;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.StoryRepository;
import com.diana.storyplanner.repository.UserRepository;
import com.diana.storyplanner.repository.CharacterRepository;
import com.diana.storyplanner.repository.EventRepository;
import com.diana.storyplanner.repository.EventLinkRepository;
import com.diana.storyplanner.repository.EventCharacterRepository;
import com.diana.storyplanner.repository.CharacterRelationshipRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления историями.
 * Является основным сервисом приложения, так как история — главная сущность.
 * Предоставляет CRUD операции для историй и каскадное удаление всех связанных данных.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final EventRepository eventRepository;
    private final EventLinkRepository eventLinkRepository;
    private final EventCharacterRepository eventCharacterRepository;
    private final CharacterRelationshipRepository relationshipRepository;

    /**
     * Получение текущего авторизованного пользователя из контекста Security.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    /**
     * Поиск истории по ID с проверкой принадлежности текущему пользователю.
     * Используется другими сервисами для проверки прав доступа.
     */
    public Story findStoryByIdAndUser(Long id) {
        User currentUser = getCurrentUser();
        return storyRepository.findById(id)
                .filter(story -> story.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("История не найдена или доступ запрещен"));
    }

    /**
     * Создание новой истории для текущего пользователя.
     */
    @Transactional
    public StoryResponse createStory(StoryRequest request) {
        User currentUser = getCurrentUser();

        Story story = Story.builder()
                .title(request.getTitle())
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .genre(request.getGenre())
                .coverUrl(request.getCoverUrl())
                .user(currentUser)
                .build();

        story = storyRepository.save(story);
        log.info("Создана история: id={}, title={}, user={}", story.getId(), story.getTitle(), currentUser.getUsername());

        return mapToResponse(story);
    }

    /**
     * Получение всех историй текущего пользователя.
     */
    public List<StoryResponse> getUserStories() {
        User currentUser = getCurrentUser();
        List<Story> stories = storyRepository.findByUser(currentUser);
        log.info("Получено {} историй для пользователя: {}", stories.size(), currentUser.getUsername());

        return stories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение истории по ID с проверкой доступа.
     */
    public StoryResponse getStoryById(Long id) {
        Story story = findStoryByIdAndUser(id);
        log.info("Получена история: id={}, title={}", story.getId(), story.getTitle());
        return mapToResponse(story);
    }

    /**
     * Обновление данных истории.
     */
    @Transactional
    public StoryResponse updateStory(Long id, StoryRequest request) {
        Story story = findStoryByIdAndUser(id);

        story.setTitle(request.getTitle());
        story.setShortDescription(request.getShortDescription());
        story.setFullDescription(request.getFullDescription());
        story.setGenre(request.getGenre());
        story.setCoverUrl(request.getCoverUrl());

        story = storyRepository.save(story);
        log.info("Обновлена история: id={}, title={}", story.getId(), story.getTitle());

        return mapToResponse(story);
    }

    /**
     * Удаление истории и всех связанных данных (каскадное удаление).
     * Удаляются:
     * - все события и их связи (event_links, event_characters)
     * - все персонажи и их связи (character_relationships, relationship_history)
     */
    @Transactional
    public void deleteStory(Long id) {
        Story story = findStoryByIdAndUser(id);
        String storyTitle = story.getTitle();

        // 1. Удаление связей событий (event_links)
        List<Event> events = eventRepository.findByStory(story);
        for (Event event : events) {
            eventLinkRepository.deleteAll(eventLinkRepository.findByFromEvent(event));
            eventLinkRepository.deleteAll(eventLinkRepository.findByToEvent(event));
        }

        // 2. Удаление связей событий с персонажами (event_characters)
        for (Event event : events) {
            eventCharacterRepository.deleteAll(eventCharacterRepository.findByEvent(event));
        }

        // 3. Удаление самих событий
        eventRepository.deleteAll(events);

        // 4. Удаление связей между персонажами (character_relationships)
        relationshipRepository.deleteAll(relationshipRepository.findByStory(story));

        // 5. Удаление всех персонажей
        List<Character> characters = characterRepository.findByStory(story);
        characterRepository.deleteAll(characters);

        // 6. Удаление самой истории
        storyRepository.delete(story);

        log.info("Удалена история: id={}, title={} и все связанные данные ({} событий, {} персонажей)",
                id, storyTitle, events.size(), characters.size());
    }

    /**
     * Преобразование сущности Story в StoryResponse DTO.
     * Подсчитывает количество персонажей и событий в истории.
     */
    private StoryResponse mapToResponse(Story story) {
        int charactersCount = characterRepository.findByStoryId(story.getId()).size();
        int eventsCount = eventRepository.findByStoryId(story.getId()).size();

        return StoryResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .shortDescription(story.getShortDescription())
                .fullDescription(story.getFullDescription())
                .genre(story.getGenre())
                .coverUrl(story.getCoverUrl())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .charactersCount(charactersCount)
                .eventsCount(eventsCount)
                .build();
    }
}