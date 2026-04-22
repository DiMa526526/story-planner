package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.LoginRequest;
import com.diana.storyplanner.dto.request.RecoverPasswordRequest;
import com.diana.storyplanner.dto.request.RegisterRequest;
import com.diana.storyplanner.dto.response.AuthResponse;
import com.diana.storyplanner.dto.response.RecoverPasswordResponse;
import com.diana.storyplanner.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер аутентификации пользователей.
 * Обрабатывает запросы регистрации, входа в систему,
 * восстановления пароля и отправки ссылки для восстановления.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Регистрация нового пользователя.
     * Проверяет уникальность username и email,
     * создаёт нового пользователя и возвращает JWT токен.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Регистрация пользователя: username={}, email={}",
                request.getUsername() != null ? request.getUsername() : "null",
                request.getEmail() != null ? request.getEmail() : "null");

        AuthResponse response = authService.register(request);

        log.info("Пользователь успешно зарегистрирован: username={}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Аутентификация пользователя (вход в систему).
     * Проверяет логин/пароль и возвращает JWT токен для дальнейших запросов.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Попытка входа: username/email={}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        log.info("Пользователь успешно вошел: username/email={}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Восстановление пароля по токену.
     * Пользователь переходит по ссылке из email и вводит новый пароль.
     */
    @PostMapping("/recover-password")
    public ResponseEntity<RecoverPasswordResponse> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request) {
        log.debug("Запрос на восстановление пароля через токен");
        RecoverPasswordResponse response = authService.recoverPassword(request);
        log.info("Пароль успешно обновлён для токена");
        return ResponseEntity.ok(response);
    }

    /**
     * Отправка ссылки для восстановления пароля на email.
     * Генерирует JWT токен и отправляет ссылку вида:
     * http://localhost:8081/auth/reset-password?token={token}
     */
    @PostMapping("/send-recover-link")
    public ResponseEntity<String> sendRecoverLink(@RequestParam String email) {
        log.debug("Отправка ссылки восстановления на email: {}", email);
        authService.sendRecoverPasswordLink(email);
        return ResponseEntity.ok("Ссылка отправлена на " + email);
    }
}