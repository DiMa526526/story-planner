package com.diana.storyplanner.config;

import com.diana.storyplanner.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности приложения.
 * Настраивает Spring Security для работы с JWT аутентификацией.
 * Основные настройки:
 * - Отключение CSRF (для REST API)
 * - Stateless сессии (без сохранения состояния на сервере)
 * - JWT фильтр для проверки токенов
 * - Правила доступа к эндпоинтам
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Конструктор для внедрения зависимостей.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Бин для шифрования паролей.
     * Использует BCrypt - надёжный алгоритм хеширования с солью.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Бин провайдера аутентификации.
     * Настраивает DaoAuthenticationProvider с UserDetailsService и PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Бин менеджера аутентификации.
     * Используется для программной аутентификации (например, при входе).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Основная конфигурация цепочки фильтров безопасности.
     * Определяет:
     *   Отключение CSRF (для REST API это безопасно)
     *   Stateless сессии (JWT не требует хранения состояния)
     *   Провайдер аутентификации
     *   JWT фильтр перед стандартным фильтром
     *   Правила доступа к эндпоинтам
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF защиту (для REST API с JWT она не нужна)
                .csrf(csrf -> csrf.disable())

                // Устанавливаем stateless сессии (не храним состояние на сервере)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Регистрируем провайдер аутентификации
                .authenticationProvider(authenticationProvider())

                // Добавляем JWT фильтр перед стандартным фильтром аутентификации
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Настройка правил доступа к эндпоинтам
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (не требуют аутентификации)
                        .requestMatchers("/auth/**", "/error").permitAll()
                        // Все остальные эндпоинты требуют JWT токен
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}