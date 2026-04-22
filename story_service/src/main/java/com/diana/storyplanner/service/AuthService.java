package com.diana.storyplanner.service;

import com.diana.storyplanner.dto.request.LoginRequest;
import com.diana.storyplanner.dto.request.RecoverPasswordRequest;
import com.diana.storyplanner.dto.request.RegisterRequest;
import com.diana.storyplanner.dto.response.AuthResponse;
import com.diana.storyplanner.dto.response.RecoverPasswordResponse;
import com.diana.storyplanner.entity.User;
import com.diana.storyplanner.exception.*;
import com.diana.storyplanner.repository.UserRepository;
import com.diana.storyplanner.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    /**
     * Регистрация нового пользователя.
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
}