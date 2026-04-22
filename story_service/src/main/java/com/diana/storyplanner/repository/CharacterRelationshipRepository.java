package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.CharacterRelationship;
import com.diana.storyplanner.entity.Story;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью CharacterRelationship (связи между персонажами).
 * Предоставляет методы для поиска связей по истории.
 */
public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, Long> {

    /**
     * Поиск всех связей между персонажами в указанной истории.
     */
    List<CharacterRelationship> findByStory(Story story);

    /**
     * Поиск всех связей между персонажами по ID истории.
     */
    List<CharacterRelationship> findByStoryId(Long storyId);
}