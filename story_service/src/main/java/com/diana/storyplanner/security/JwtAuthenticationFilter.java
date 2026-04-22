package com.diana.storyplanner.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT аутентификационный фильтр.
 * Перехватывает каждый HTTP запрос, проверяет наличие JWT токена
 * в заголовке Authorization и устанавливает аутентификацию в SecurityContext.
 * Фильтр выполняется один раз на каждый запрос (extends OncePerRequestFilter).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Основной метод фильтрации запросов.
     * Выполняется для каждого HTTP запроса.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Извлечение заголовка Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Проверка наличия заголовка и формата Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Извлечение токена из заголовка (удаляем префикс "Bearer ")
        final String token = authHeader.substring(7);

        try {
            // 4. Извлечение email из токена
            final String userEmail = jwtService.extractUsername(token);

            // 5. Если email есть и пользователь ещё не аутентифицирован
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Загрузка данных пользователя из базы данных
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 7. Создание объекта аутентификации
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 8. Добавление деталей запроса
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. Установка аутентификации в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Логирование ошибки аутентификации, но не прерывание цепочки фильтров
            log.error("JWT authentication error: {}", e.getMessage());
        }

        // 10. Продолжение цепочки фильтров
        filterChain.doFilter(request, response);
    }
}