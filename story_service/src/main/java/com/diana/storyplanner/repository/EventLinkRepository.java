package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.EventLink;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью EventLink (связи между событиями).
 * Используется для построения графа сюжета (нелинейных ветвлений).
 */
public interface EventLinkRepository extends JpaRepository<EventLink, Long> {

    /**
     * Поиск всех исходящих связей из указанного события.
     * Показывает, в какие события можно перейти из данного.
     */
    List<EventLink> findByFromEvent(Event event);

    /**
     * Поиск всех входящих связей к указанному событию.
     * Показывает, из каких событий можно прийти в данное.
     */
    List<EventLink> findByToEvent(Event event);
}