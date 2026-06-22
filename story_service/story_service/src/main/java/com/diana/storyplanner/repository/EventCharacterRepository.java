package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.Event;
import com.diana.storyplanner.entity.EventCharacter;
import com.diana.storyplanner.entity.EventCharacterId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью EventCharacter (связь событие-персонаж).
 * Использует составной ключ EventCharacterId.
 */
public interface EventCharacterRepository extends JpaRepository<EventCharacter, EventCharacterId> {

    /**
     * Поиск всех связей для указанного события.
     * Возвращает всех персонажей, участвующих в событии.
     */
    List<EventCharacter> findByEvent(Event event);
}