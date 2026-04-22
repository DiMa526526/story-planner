package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Связь между двумя персонажами.
 * Определяет, что два персонажа имеют какие-либо отношения в истории.
 * История отношений отслеживается через RelationshipHistory.
 */
@Entity
@Table(name = "character_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterRelationship {

    /**
     * Уникальный идентификатор связи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * История, к которой относится связь.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    /**
     * Первый персонаж в отношениях.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character1_id", nullable = false)
    private Character character1;

    /**
     * Второй персонаж в отношениях.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character2_id", nullable = false)
    private Character character2;
}