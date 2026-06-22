package com.diana.storyplanner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Отправка письма для восстановления пароля.
     */
    public void sendRecoverPasswordEmail(String to, String token) {
        String link = "http://localhost:5173/#/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Восстановление пароля");
        message.setText("Перейдите по ссылке, чтобы восстановить пароль:\n" + link);
        mailSender.send(message);
        log.info("Отправлено письмо для восстановления пароля на: {}", to);
    }

    /**
     * Отправка шестизначного кода подтверждения для регистрации.
     */
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Подтверждение регистрации - Story Planner");
        message.setText(String.format("""
            Здравствуйте!
            
            Ваш код подтверждения для регистрации в Story Planner: %s
            
            Код действителен в течение 15 минут.
            
            Если вы не регистрировались в нашем сервисе, просто проигнорируйте это письмо.
            
            С уважением,
            Команда Story Planner
            """, code));
        mailSender.send(message);
        log.info("Отправлен код подтверждения на: {}", to);
    }
}