package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.LoginRequest;
import com.diana.storyplanner.dto.request.RecoverPasswordRequest;
import com.diana.storyplanner.dto.request.RegisterRequest;
import com.diana.storyplanner.dto.request.VerifyCodeRequest;
import com.diana.storyplanner.dto.response.AuthResponse;
import com.diana.storyplanner.dto.response.RecoverPasswordResponse;
import com.diana.storyplanner.entity.User;
import com.diana.storyplanner.exception.*;
import com.diana.storyplanner.repository.UserRepository;
import com.diana.storyplanner.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис аутентификации пользователей.
 * Содержит бизнес-логику регистрации, входа и восстановления пароля через JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    // Временное хранилище для кодов подтверждения (в памяти)
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.verification-code-expiry-minutes:15}")
    private int codeExpiryMinutes;

    /**
     * Отправка шестизначного кода подтверждения на email.
     * Пользователь ещё НЕ создаётся в БД.
     */
    public void sendVerificationCode(String email) {
        // Проверяем, не существует ли уже пользователь с таким email
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        // Генерируем 6-значный код
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        // Сохраняем код временно в памяти
        pendingRegistrations.put(email, new PendingRegistration(code));

        // Отправляем код на email
        emailService.sendVerificationCode(email, code);

        // Очищаем просроченные записи
        cleanupExpiredRegistrations();

        log.info("Код подтверждения отправлен на email: {}", email);
    }

    /**
     * Проверка кода и завершение регистрации.
     */
    public AuthResponse verifyCodeAndRegister(RegisterRequest request, String code) {
        // Проверяем временную регистрацию
        PendingRegistration pending = pendingRegistrations.get(request.getEmail());

        if (pending == null) {
            throw new InvalidCodeException("Код не найден. Запросите новый код подтверждения");
        }

        if (pending.isExpired(codeExpiryMinutes)) {
            pendingRegistrations.remove(request.getEmail());
            throw new InvalidCodeException("Срок действия кода истёк. Запросите новый код");
        }

        if (!pending.getCode().equals(code)) {
            throw new InvalidCodeException("Неверный код подтверждения");
        }

        // Ещё раз проверяем, не создан ли уже пользователь
        if (userRepository.existsByEmail(request.getEmail())) {
            pendingRegistrations.remove(request.getEmail());
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            pendingRegistrations.remove(request.getEmail());
            throw new UserAlreadyExistsException("Пользователь с таким username уже существует");
        }

        // Создаём пользователя ТОЛЬКО сейчас
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        // Удаляем временную запись
        pendingRegistrations.remove(request.getEmail());

        log.info("Пользователь зарегистрирован: id={}, username={}", user.getId(), user.getUsername());

        // Генерируем JWT для входа
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse("Регистрация успешна", token);
    }

    /**
     * Очистка просроченных регистраций.
     */
    private void cleanupExpiredRegistrations() {
        pendingRegistrations.entrySet().removeIf(entry -> entry.getValue().isExpired(codeExpiryMinutes));
    }

    /**
     * Регистрация нового пользователя (простая версия, без подтверждения).
     * Оставлена для обратной совместимости.
     */
    public AuthResponse register(RegisterRequest request) {
        log.debug("Начало регистрации пользователя: email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким username уже существует");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        log.info("Пользователь зарегистрирован: id={}, username={}", user.getId(), user.getUsername());

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse("Регистрация успешна", token);
    }

    /**
     * Вход пользователя в систему.
     */
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailOrUsername(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Неверный пароль");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse("Вход выполнен успешно", token);
    }

    /**
     * Генерация токена для восстановления пароля.
     */
    public String generateRecoverPasswordToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        return jwtService.generateToken(user.getEmail());
    }

    /**
     * Восстановление пароля по токену.
     */
    public RecoverPasswordResponse recoverPassword(RecoverPasswordRequest request) {

        log.debug("Восстановление пароля через токен");

        String email;

        try {
            email = jwtService.extractUsername(request.getToken());
        } catch (Exception e) {
            throw new InvalidTokenException("Неверный или просроченный токен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Пароль обновлён для пользователя: id={}", user.getId());

        return new RecoverPasswordResponse("Пароль успешно обновлён");
    }

    /**
     * Отправка ссылки для восстановления пароля.
     */
    public void sendRecoverPasswordLink(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь не найден. Проверьте введённый email"
                ));

        String token = jwtService.generateToken(user.getEmail());

        emailService.sendRecoverPasswordEmail(email, token);

        log.info("Ссылка для восстановления пароля отправлена на {}", email);
    }

    /**
     * Вспомогательный класс для хранения кода и времени создания.
     */
    private static class PendingRegistration {
        private final String code;
        private final Instant createdAt;

        public PendingRegistration(String code) {
            this.code = code;
            this.createdAt = Instant.now();
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired(int expiryMinutes) {
            return Instant.now().isAfter(createdAt.plusSeconds(expiryMinutes * 60L));
        }
    }
}