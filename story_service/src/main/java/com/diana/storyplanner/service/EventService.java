package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.EventRequest;
import com.diana.storyplanner.dto.response.EventResponse;
import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.entity.EventCharacter;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.EventRepository;
import com.diana.storyplanner.repository.CharacterRepository;
import com.diana.storyplanner.repository.EventCharacterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления событиями в истории.
 * Предоставляет CRUD операции для событий,
 * управляет связями между событиями и персонажами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final StoryService storyService;
    private final CharacterRepository characterRepository;
    private final EventCharacterRepository eventCharacterRepository;

    /**
     * Поиск события по ID с проверкой прав доступа.
     * Проверяет, что событие принадлежит истории текущего пользователя.
     */
    public Event findEventByIdAndUser(Long id) {
        return eventRepository.findById(id)
                .filter(event -> event.getStory().getUser() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено или доступ запрещен"));
    }

    /**
     * Создание нового события в указанной истории.
     * Сначала проверяет доступ пользователя к истории,
     * затем создаёт событие и связывает его с указанными персонажами.
     * Дубликаты персонажей автоматически удаляются.
     */
    @Transactional
    public EventResponse createEvent(Long storyId, EventRequest request) {
        // Проверка доступа к истории
        Story story = storyService.findStoryByIdAndUser(storyId);

        // Создание нового события
        Event event = Event.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .story(story)
                .build();

        event = eventRepository.save(event);

        // Связывание события с персонажами (без дубликатов)
        if (request.getCharacterIds() != null && !request.getCharacterIds().isEmpty()) {
            // Удаление дубликатов из запроса
            List<Long> uniqueCharacterIds = request.getCharacterIds().stream().distinct().toList();

            for (Long characterId : uniqueCharacterIds) {
                Character character = characterRepository.findById(characterId)
                        .orElseThrow(() -> new ResourceNotFoundException("Персонаж не найден: " + characterId));

                // Проверка, не добавлен ли уже этот персонаж
                boolean alreadyExists = eventCharacterRepository.findByEvent(event).stream()
                        .anyMatch(ec -> ec.getCharacter().getId().equals(characterId));

                if (!alreadyExists) {
                    EventCharacter eventCharacter = new EventCharacter();
                    eventCharacter.setEvent(event);
                    eventCharacter.setCharacter(character);
                    eventCharacterRepository.save(eventCharacter);
                }
            }
        }

        log.info("Создано событие: id={}, title={}, storyId={}", event.getId(), event.getTitle(), storyId);

        return mapToResponse(event);
    }

    /**
     * Получение всех событий указанной истории.
     */
    public List<EventResponse> getEventsByStory(Long storyId) {
        storyService.findStoryByIdAndUser(storyId);
        return eventRepository.findByStoryId(storyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение события по ID с проверкой доступа.
     */
    public EventResponse getEventById(Long id) {
        Event event = findEventByIdAndUser(id);
        return mapToResponse(event);
    }

    /**
     * Обновление данных события.
     * Обновляет название, содержание и список связанных персонажей.
     * Старые связи с персонажами полностью заменяются новыми.
     */
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = findEventByIdAndUser(id);

        // Обновление основных полей
        event.setTitle(request.getTitle());
        event.setContent(request.getContent());

        // Обновление связей с персонажами
        if (request.getCharacterIds() != null) {
            // Удаление старых связей
            List<EventCharacter> oldLinks = eventCharacterRepository.findByEvent(event);
            eventCharacterRepository.deleteAll(oldLinks);

            // Добавление новых связей
            for (Long characterId : request.getCharacterIds()) {
                Character character = characterRepository.findById(characterId)
                        .orElseThrow(() -> new ResourceNotFoundException("Персонаж не найден: " + characterId));

                EventCharacter eventCharacter = new EventCharacter();
                eventCharacter.setEvent(event);
                eventCharacter.setCharacter(character);
                eventCharacterRepository.save(eventCharacter);
            }
        }

        event = eventRepository.save(event);
        log.info("Обновлено событие: id={}", id);

        return mapToResponse(event);
    }

    /**
     * Удаление события.
     * При удалении события автоматически удаляются:
     * - связи события с персонажами (event_characters)
     * - связи события с другими событиями (event_links)
     */
    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventByIdAndUser(id);
        eventRepository.delete(event);
        log.info("Удалено событие: id={}", id);
    }

    /**
     * Преобразование сущности Event в EventResponse DTO.
     * Включает в ответ список персонажей, участвующих в событии.
     */
    private EventResponse mapToResponse(Event event) {
        // Получение списка персонажей события
        List<CharacterResponse> characters = eventCharacterRepository.findByEvent(event)
                .stream()
                .map(ec -> CharacterResponse.builder()
                        .id(ec.getCharacter().getId())
                        .name(ec.getCharacter().getName())
                        .description(ec.getCharacter().getDescription())
                        .imageUrl(ec.getCharacter().getImageUrl())
                        .storyId(event.getStory().getId())
                        .build())
                .toList();

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .characters(characters)
                .outgoingLinks(null) // Ссылки заполняются отдельно в EventLinkService
                .build();
    }
}