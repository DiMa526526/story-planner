package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.Story;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Event (событие).
 * Предоставляет методы для поиска событий по истории.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Поиск всех событий в указанной истории.
     */
    List<Event> findByStory(Story story);

    /**
     * Поиск всех событий по ID истории.
     */
    List<Event> findByStoryId(Long storyId);
}