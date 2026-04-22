package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.EventLinkRequest;
import com.diana.storyplanner.dto.response.EventLinkResponse;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.EventLink;
import com.diana.storyplanner.exception.DuplicateRelationshipException;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.exception.SelfLinkException;
import com.diana.storyplanner.repository.EventLinkRepository;
import com.diana.storyplanner.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления связями между событиями (граф сюжета).
 * Позволяет создавать нелинейные ветвления в истории,
 * где выбор пользователя влияет на развитие сюжета.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLinkService {

    private final EventLinkRepository eventLinkRepository;
    private final EventRepository eventRepository;

    /**
     * Поиск события по ID с проверкой прав доступа.
     * Проверяет, что событие принадлежит истории текущего пользователя.
     */
    private Event findEventByIdAndUser(Long id) {
        return eventRepository.findById(id)
                .filter(event -> event.getStory().getUser() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено или доступ запрещен"));
    }

    /**
     * Создание связи между событиями.
     * Определяет, из какого события (fromEvent) можно перейти в какое (toEvent).
     */
    @Transactional
    public EventLinkResponse createEventLink(Long fromEventId, EventLinkRequest request) {
        // Запрет на создание связи события с самим собой
        if (fromEventId.equals(request.getToEventId())) {
            throw new SelfLinkException("Нельзя создать связь события с самим собой");
        }

        Event fromEvent = findEventByIdAndUser(fromEventId);
        Event toEvent = eventRepository.findById(request.getToEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Целевое событие не найдено"));

        // Проверка на существование такой связи
        boolean exists = eventLinkRepository.findByFromEvent(fromEvent).stream()
                .anyMatch(link -> link.getToEvent().getId().equals(request.getToEventId()));

        if (exists) {
            throw new DuplicateRelationshipException("Связь между этими событиями уже существует");
        }

        // Проверка принадлежности к одной истории
        if (!fromEvent.getStory().getId().equals(toEvent.getStory().getId())) {
            throw new IllegalArgumentException("События должны принадлежать одной истории");
        }

        // Создание связи
        EventLink eventLink = EventLink.builder()
                .fromEvent(fromEvent)
                .toEvent(toEvent)
                .choiceText(request.getChoiceText())
                .build();

        eventLink = eventLinkRepository.save(eventLink);
        log.info("Создана связь событий: from={}, to={}", fromEventId, request.getToEventId());

        return mapToResponse(eventLink);
    }

    /**
     * Обновление существующей связи между событиями.
     * Позволяет изменить целевое событие или текст выбора.
     */
    @Transactional
    public EventLinkResponse updateEventLink(Long linkId, EventLinkRequest request) {
        EventLink eventLink = eventLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Связь не найдена"));

        // Проверка доступа
        findEventByIdAndUser(eventLink.getFromEvent().getId());

        // Запрет на связь с самим собой
        if (eventLink.getFromEvent().getId().equals(request.getToEventId())) {
            throw new SelfLinkException("Нельзя создать связь события с самим собой");
        }

        Event toEvent = eventRepository.findById(request.getToEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Целевое событие не найдено"));

        // Проверка принадлежности к одной истории
        if (!eventLink.getFromEvent().getStory().getId().equals(toEvent.getStory().getId())) {
            throw new IllegalArgumentException("События должны принадлежать одной истории");
        }

        // Обновление связи
        eventLink.setToEvent(toEvent);
        eventLink.setChoiceText(request.getChoiceText());

        eventLink = eventLinkRepository.save(eventLink);
        log.info("Обновлена связь событий: id={}", linkId);

        return mapToResponse(eventLink);
    }

    /**
     * Получение всех исходящих связей события.
     * Показывает, в какие события можно перейти из данного.
     */
    public List<EventLinkResponse> getOutgoingLinks(Long eventId) {
        Event event = findEventByIdAndUser(eventId);
        return eventLinkRepository.findByFromEvent(event)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение всех входящих связей события.
     * Показывает, из каких событий можно прийти в данное.
     */
    public List<EventLinkResponse> getIncomingLinks(Long eventId) {
        Event event = findEventByIdAndUser(eventId);
        return eventLinkRepository.findByToEvent(event)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Удаление связи между событиями.
     */
    @Transactional
    public void deleteEventLink(Long linkId) {
        EventLink eventLink = eventLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Связь не найдена"));

        // Проверка доступа
        findEventByIdAndUser(eventLink.getFromEvent().getId());

        eventLinkRepository.delete(eventLink);
        log.info("Удалена связь событий: id={}", linkId);
    }

    /**
     * Преобразование сущности EventLink в EventLinkResponse DTO.
     */
    private EventLinkResponse mapToResponse(EventLink link) {
        return EventLinkResponse.builder()
                .id(link.getId())
                .fromEventId(link.getFromEvent().getId())
                .toEventId(link.getToEvent().getId())
                .choiceText(link.getChoiceText())
                .toEventTitle(link.getToEvent().getTitle())
                .build();
    }
}