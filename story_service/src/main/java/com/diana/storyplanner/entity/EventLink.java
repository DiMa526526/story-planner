package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Связь между событиями.
 * Используется для построения нелинейного сюжета (графа).
 * Позволяет создавать ветвления в истории.
 */
@Entity
@Table(name = "event_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLink {

    /**
     * Уникальный идентификатор связи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Исходное событие (откуда идёт переход).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_event_id", nullable = false)
    private Event fromEvent;

    /**
     * Целевое событие (куда ведёт переход).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_event_id", nullable = false)
    private Event toEvent;

    /**
     * Текст выбора для пользователя.
     * Например: "Пойти налево" или "Сражаться с драконом".
     * Может быть null, если переход безальтернативный.
     */
    private String choiceText;
}