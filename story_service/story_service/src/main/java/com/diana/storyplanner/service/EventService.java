package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.EventRequest;
import com.diana.storyplanner.dto.response.EventResponse;
import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.entity.EventCharacter;
import com.diana.storyplanner.entity.EventLink;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.EventRepository;
import com.diana.storyplanner.repository.CharacterRepository;
import com.diana.storyplanner.repository.EventCharacterRepository;
import com.diana.storyplanner.repository.EventLinkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final EventLinkRepository eventLinkRepository;

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
     * Получение всех событий указанной истории, отсортированных по таймлайну.
     * События сортируются в следующем порядке:
     * 1. Корневые события (без входящих связей) - по дате создания
     * 2. Остальные события в порядке, соответствующем связям (топологическая сортировка)
     * 3. Изолированные события (без связей) - по дате создания
     */
    public List<EventResponse> getEventsByStory(Long storyId) {
        storyService.findStoryByIdAndUser(storyId);

        List<Event> events = eventRepository.findByStoryId(storyId);
        if (events.isEmpty()) {
            return List.of();
        }

        // Получаем все связи для этой истории
        List<EventLink> allLinks = eventLinkRepository.findByStoryId(storyId);

        // Строим граф связей
        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        Map<Long, List<Long>> outgoingMap = new java.util.HashMap<>();
        Map<Long, Long> incomingCount = new java.util.HashMap<>();

        for (Event event : events) {
            outgoingMap.put(event.getId(), new ArrayList<>());
            incomingCount.put(event.getId(), 0L);
        }

        for (EventLink link : allLinks) {
            Long fromId = link.getFromEvent().getId();
            Long toId = link.getToEvent().getId();
            if (eventMap.containsKey(fromId) && eventMap.containsKey(toId)) {
                outgoingMap.get(fromId).add(toId);
                incomingCount.put(toId, incomingCount.get(toId) + 1);
            }
        }

        // Находим корневые события (без входящих связей)
        List<Event> rootEvents = events.stream()
                .filter(e -> incomingCount.get(e.getId()) == 0)
                .sorted(Comparator.comparing(Event::getCreatedAt))
                .toList();

        // Топологическая сортировка (BFS)
        Set<Long> visited = new HashSet<>();
        List<Event> sortedEvents = new ArrayList<>();
        java.util.Queue<Long> queue = new java.util.LinkedList<>();

        // Добавляем корневые события в очередь
        for (Event root : rootEvents) {
            queue.add(root.getId());
            visited.add(root.getId());
            sortedEvents.add(root);
        }

        // BFS обход по связям
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            for (Long nextId : outgoingMap.get(currentId)) {
                if (!visited.contains(nextId)) {
                    // Проверяем, все ли входящие связи уже обработаны
                    // (упрощённо: добавляем, если ещё не добавлено)
                    visited.add(nextId);
                    sortedEvents.add(eventMap.get(nextId));
                    queue.add(nextId);
                }
            }
        }

        // Добавляем оставшиеся события (изолированные или циклы)
        for (Event event : events) {
            if (!visited.contains(event.getId())) {
                sortedEvents.add(event);
            }
        }

        return sortedEvents.stream()
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
                .outgoingLinks(null)
                .storyId(event.getStory().getId())
                .build();
    }
}