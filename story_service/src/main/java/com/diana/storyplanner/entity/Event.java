package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Событие в истории.
 * Используется для построения сюжета и графа событий.
 * Может быть связано с персонажами и другими событиями.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    /**
     * Уникальный идентификатор события.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * История, к которой принадлежит событие.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    /**
     * Название события.
     */
    @Column(nullable = false)
    @NotBlank(message = "Название события обязательно")
    private String title;

    /**
     * Содержание события (описание, диалоги, действия).
     */
    private String content;

    /**
     * Дата создания события.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления события.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}