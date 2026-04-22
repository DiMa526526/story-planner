package com.diana.storyplanner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки email сообщений.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправка письма для восстановления пароля.
     */
    public void sendRecoverPasswordEmail(String to, String token) {

        String link = "http://localhost:8081/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Восстановление пароля");
        message.setText("Перейдите по ссылке, чтобы восстановить пароль:\n" + link);

        mailSender.send(message);

        log.info("Письмо отправлено на {}", to);
    }
}