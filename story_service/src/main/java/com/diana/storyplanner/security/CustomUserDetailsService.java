package com.diana.storyplanner.security;

import com.diana.storyplanner.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Реализация интерфейса UserDetailsService для Spring Security.
 * Отвечает за загрузку данных пользователя из базы данных по email.
 * Используется при JWT аутентификации для получения информации о пользователе.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по email для аутентификации.
     * Используется JwtAuthenticationFilter для получения информации о пользователе из токена.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Поиск пользователя в базе данных по email
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Возвращаем объект UserDetails с email в качестве username,
        // хешем пароля и ролью "USER"
        return User.builder()
                .username(user.getEmail())          // email используется как username
                .password(user.getPasswordHash())   // зашифрованный пароль
                .authorities("USER")                // роль пользователя
                .build();
    }
}