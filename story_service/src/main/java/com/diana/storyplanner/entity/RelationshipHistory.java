package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * История изменений взаимоотношений между персонажами.
 * Позволяет отслеживать эволюцию отношений в процессе сюжета.
 * Каждая запись привязана к конкретному событию.
 */
@Entity
@Table(name = "relationship_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationshipHistory {

    /**
     * Уникальный идентификатор записи истории.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь между персонажами, к которой относится запись.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false)
    private CharacterRelationship relationship;

    /**
     * Событие, в котором произошло изменение отношений.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * Тип отношений (пользовательское значение).
     * Например: "друзья", "враги", "союзники".
     */
    @Column(nullable = false)
    @NotBlank(message = "Тип отношения обязателен")
    private String relationshipType;

    /**
     * Цвет для визуализации связи (HEX-код).
     * Например: "#00FF00" для друзей, "#FF0000" для врагов.
     */
    private String color;
}