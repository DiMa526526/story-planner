package com.diana.storyplanner.entity;

import lombok.*;
import java.io.Serializable;

/**
 * Составной ключ для связи событие-персонаж (EventCharacter).
 * Реализует Serializable для использования в JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventCharacterId implements Serializable {

    /**
     * ID события.
     * Должен совпадать с типом поля id в Event.
     */
    private Long event;

    /**
     * ID персонажа.
     * Должен совпадать с типом поля id в Character.
     */
    private Long character;
}