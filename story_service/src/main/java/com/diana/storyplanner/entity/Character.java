package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Сущность персонажа.
 * Хранит информацию о героях истории: имя, описание, изображение.
 * Связан с событиями через отношение many-to-many.
 */
@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character {

    /**
     * Уникальный идентификатор персонажа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * История, к которой принадлежит персонаж.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    /**
     * Имя персонажа.
     */
    @Column(nullable = false)
    @NotBlank(message = "Имя персонажа обязательно")
    private String name;

    /**
     * Описание персонажа (характер, внешность, биография).
     */
    private String description;

    /**
     * URL изображения персонажа.
     */
    @Column(name = "image_url")
    private String imageUrl;
}