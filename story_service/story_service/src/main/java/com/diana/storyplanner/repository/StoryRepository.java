package com.diana.storyplanner.repository;

import com.diana.storyplanner.entity.Story;
import com.diana.storyplanner.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Story (история).
 * Основной репозиторий приложения, так как история — главная сущность.
 */
public interface StoryRepository extends JpaRepository<Story, Long> {

    /**
     * Поиск всех историй, принадлежащих указанному пользователю.
     */
    List<Story> findByUser(User user);

    /**
     * Поиск всех историй по ID пользователя.
     */
    List<Story> findByUserId(Long userId);
}