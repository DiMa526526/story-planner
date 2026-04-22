package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Связь между событиями и персонажами (многие-ко-многим).
 * Определяет, какие персонажи участвуют в данном событии.
 */
@Entity
@Table(name = "event_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EventCharacterId.class)
public class EventCharacter {

    /**
     * Событие, в котором участвует персонаж.
     * Часть составного первичного ключа.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    /**
     * Персонаж, участвующий в событии.
     * Часть составного первичного ключа.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;
}