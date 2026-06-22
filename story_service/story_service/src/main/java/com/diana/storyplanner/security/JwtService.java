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

    /**
     * Проверка, истёк ли срок действия токена.
     *
     * @param token JWT токен
     * @return true если токен истёк, false в противном случае
     */
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Валидация JWT токена.
     * Проверяет корректность подписи и срок действия токена.
     * Используется для проверки авторизации при доступе к защищённым ресурсам.
     *
     * @param token JWT токен
     * @return true если токен валиден (корректная подпись и не истёк), false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            // Пытаемся распарсить токен (проверка подписи и структуры)
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // Проверяем, не истёк ли срок действия токена
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Любая ошибка при парсинге токена означает, что он невалиден
            return false;
        }
    }
}