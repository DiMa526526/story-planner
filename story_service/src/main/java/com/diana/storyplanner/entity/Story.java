package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Сущность истории.
 * Основная сущность проекта — хранит сюжет, описание и принадлежит пользователю.
 * Содержит персонажей (Character), события (Event) и связи между ними.
 */
@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    /**
     * Уникальный идентификатор истории.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец истории.
     * Связь многие-к-одному с пользователем.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Название истории.
     */
    @Column(nullable = false)
    @NotBlank(message = "Название обязательно")
    private String title;

    /**
     * Краткое описание истории (до 500 символов).
     */
    @Column(name = "short_description")
    private String shortDescription;

    /**
     * Полное описание истории (текст без ограничений).
     */
    @Column(name = "full_description")
    private String fullDescription;

    /**
     * Жанр истории.
     * Хранится как строка в базе данных.
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private Genre genre;

    /**
     * URL обложки истории.
     */
    @Column(name = "cover_url")
    private String coverUrl;

    /**
     * Дата создания истории.
     * Заполняется автоматически, не обновляется.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления истории.
     * Обновляется автоматически при каждом изменении.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}