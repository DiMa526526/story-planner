package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.Character;
import com.diana.storyplanner.entity.Story;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Character (персонаж).
 * Предоставляет методы для поиска персонажей по истории.
 */
public interface CharacterRepository extends JpaRepository<Character, Long> {

    /**
     * Поиск всех персонажей в указанной истории.
     */
    List<Character> findByStory(Story story);

    /**
     * Поиск всех персонажей по ID истории.
     */
    List<Character> findByStoryId(Long storyId);
}