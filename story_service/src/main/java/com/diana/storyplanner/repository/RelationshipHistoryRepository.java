package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.CharacterRelationship;
import com.diana.storyplanner.entity.RelationshipHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью RelationshipHistory (история отношений).
 * Позволяет отслеживать изменения отношений персонажей во времени.
 */
public interface RelationshipHistoryRepository extends JpaRepository<RelationshipHistory, Long> {

    /**
     * Поиск всей истории изменений для указанной связи между персонажами.
     * Возвращает записи в порядке их создания.
     */
    List<RelationshipHistory> findByRelationship(CharacterRelationship relationship);
}