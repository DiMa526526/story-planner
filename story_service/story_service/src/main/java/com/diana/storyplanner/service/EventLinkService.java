package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.EventLinkRequest;
import com.diana.storyplanner.dto.response.EventLinkResponse;
import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.EventLink;
import com.diana.storyplanner.exception.ResourceNotFoundException;
import com.diana.storyplanner.repository.EventLinkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления связями между событиями.
 * Позволяет строить нелинейные сюжеты с ветвлениями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLinkService {

    private final EventLinkRepository eventLinkRepository;
    private final EventService eventService;

    /**
     * Поиск связи по ID с проверкой прав доступа.
     */
    public EventLink findEventLinkByIdAndUser(Long id) {
        return eventLinkRepository.findById(id)
                .filter(link -> link.getFromEvent().getStory().getUser() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Связь не найдена или доступ запрещен"));
    }

    /**
     * Создание связи между двумя событиями.
     * Проверяет, что оба события существуют и принадлежат одной истории.
     */
    @Transactional
    public EventLinkResponse createLink(Long fromEventId, EventLinkRequest request) {
        Event fromEvent = eventService.findEventByIdAndUser(fromEventId);
        Event toEvent = eventService.findEventByIdAndUser(request.getToEventId());

        // Проверка, что оба события принадлежат одной истории
        if (!fromEvent.getStory().getId().equals(toEvent.getStory().getId())) {
            throw new IllegalArgumentException("События должны принадлежать одной истории");
        }

        // Запрет на создание циклической ссылки
        if (fromEventId.equals(request.getToEventId())) {
            throw new IllegalArgumentException("Нельзя создать ссылку события на само себя");
        }

        // Проверка на существование такой же связи
        boolean exists = eventLinkRepository.findByFromEvent(fromEvent).stream()
                .anyMatch(link -> link.getToEvent().getId().equals(request.getToEventId()));
        if (exists) {
            throw new IllegalArgumentException("Связь между этими событиями уже существует");
        }

        EventLink link = EventLink.builder()
                .fromEvent(fromEvent)
                .toEvent(toEvent)
                .choiceText(request.getChoiceText())
                .build();

        link = eventLinkRepository.save(link);
        log.info("Создана связь: из события {} в событие {}", fromEventId, request.getToEventId());

        return mapToResponse(link);
    }

    /**
     * Обновление существующей связи между событиями.
     */
    @Transactional
    public EventLinkResponse updateLink(Long linkId, EventLinkRequest request) {
        EventLink link = findEventLinkByIdAndUser(linkId);

        // Проверка доступа к целевому событию
        Event toEvent = eventService.findEventByIdAndUser(request.getToEventId());

        // Проверка, что целевое событие принадлежит той же истории
        if (!toEvent.getStory().getId().equals(link.getFromEvent().getStory().getId())) {
            throw new IllegalArgumentException("Целевое событие должно принадлежать той же истории");
        }

        // Запрет на создание циклической ссылки (нельзя ссылаться на себя)
        if (link.getFromEvent().getId().equals(request.getToEventId())) {
            throw new IllegalArgumentException("Нельзя создать ссылку события на само себя");
        }

        link.setToEvent(toEvent);
        link.setChoiceText(request.getChoiceText());

        link = eventLinkRepository.save(link);
        log.info("Обновлена связь событий: id={}, from={}, to={}",
                link.getId(), link.getFromEvent().getId(), link.getToEvent().getId());

        return mapToResponse(link);
    }

    /**
     * Получение всех исходящих связей из указанного события.
     */
    public List<EventLinkResponse> getOutgoingLinks(Long eventId) {
        Event event = eventService.findEventByIdAndUser(eventId);
        return eventLinkRepository.findByFromEvent(event)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получение всех входящих связей к указанному событию.
     */
    public List<EventLinkResponse> getIncomingLinks(Long eventId) {
        Event event = eventService.findEventByIdAndUser(eventId);
        return eventLinkRepository.findByToEvent(event)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Удаление связи между событиями.
     */
    @Transactional
    public void deleteLink(Long linkId) {
        EventLink link = findEventLinkByIdAndUser(linkId);
        eventLinkRepository.delete(link);
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
                .toEventTitle(link.getToEvent().getTitle())
                .choiceText(link.getChoiceText())
                .build();
    }
}