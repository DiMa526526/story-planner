package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User (пользователь).
 * Предоставляет методы для поиска пользователей по email, username
 * и проверки их уникальности при регистрации.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по email.
     * Используется при входе и восстановлении пароля.
     */
    Optional<User> findByEmail(String email);

    /**
     * Поиск пользователя по username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск пользователя по email ИЛИ username.
     * Удобно для входа, когда пользователь может ввести любое значение.
     */
    Optional<User> findByEmailOrUsername(String email, String username);

    /**
     * Проверка существования пользователя с указанным email.
     * Используется при регистрации для проверки уникальности.
     */
    boolean existsByEmail(String email);

    /**
     * Проверка существования пользователя с указанным username.
     * Используется при регистрации для проверки уникальности.
     */
    boolean existsByUsername(String username);
}