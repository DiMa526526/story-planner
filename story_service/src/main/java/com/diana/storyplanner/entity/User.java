package com.diana.storyplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

/**
 * Сущность пользователя.
 * Хранит данные для авторизации и идентификации пользователя в системе.
 * Связана с историями (Story) через отношение one-to-many.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя (логин).
     * Должно быть уникальным, не более 25 символов.
     */
    @Column(length = 25, nullable = false, unique = true)
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    /**
     * Электронная почта пользователя.
     * Используется для входа и восстановления пароля.
     */
    @Column(nullable = false, unique = true)
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;

    /**
     * Хеш пароля пользователя.
     * Хранится в зашифрованном виде (BCrypt).
     */
    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Пароль обязателен")
    private String passwordHash;

    /**
     * Дата и время создания аккаунта.
     * Заполняется автоматически при сохранении.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}