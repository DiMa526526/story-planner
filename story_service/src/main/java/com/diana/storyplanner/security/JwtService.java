package com.diana.storyplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Сервис для генерации и валидации JWT (JSON Web Token) токенов.
 * Используется для аутентификации пользователей без сохранения состояния (stateless).
 * Токен содержит email пользователя и срок действия 1 час.
 */
@Service
public class JwtService {

    /**
     * Секретный ключ для подписи JWT токенов.
     * Генерируется автоматически при запуске приложения.
     * В production рекомендуется выносить в конфигурацию.
     */
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Время жизни токена в миллисекундах.
     * 1 час = 3600000 миллисекунд.
     */
    private final long EXPIRATION = 3600_000; // 1 час

    /**
     * Генерация JWT токена по email пользователя.
     * Токен содержит:
     * - subject: email пользователя
     * - issuedAt: время создания
     * - expiration: время истечения (через 1 час)
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(email)          // email пользователя
                .setIssuedAt(now)           // время создания
                .setExpiration(expiryDate)  // время истечения
                .signWith(key)              // подпись токена
                .compact();
    }

    /**
     * Извлечение email из JWT токена.
     * Также выполняет валидацию подписи токена.
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)         // ключ для проверки подписи
                .build()
                .parseClaimsJws(token)      // парсинг и валидация токена
                .getBody()
                .getSubject();              // извлечение email из subject
    }
}