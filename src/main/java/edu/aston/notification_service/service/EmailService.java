package edu.aston.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    /**
     * Отправляет уведомление на email в зависимости от операции
     * @param email Email получателя
     * @param operation Тип операции (CREATED или DELETED)
     */
    public void sendNotificationEmail(String email, String operation) {
        log.info("Подготовка к отправке email для {} с операцией: {}", email, operation);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        
        if ("CREATED".equals(operation)) {
            message.setSubject("Добро пожаловать на наш сайт!");
            message.setText("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.\n\n" +
                           "С уважением,\nКоманда поддержки");
        } else if ("DELETED".equals(operation)) {
            message.setSubject("Ваш аккаунт был удален");
            message.setText("Здравствуйте! Ваш аккаунт был удалён.\n\n" +
                           "С уважением,\nКоманда поддержки");
        } else {
            log.warn("Неизвестная операция: {}. Email не отправлен.", operation);
            return;
        }
        
        try {
            mailSender.send(message);
            log.info("Email успешно отправлен на адрес: {}", email);
        } catch (Exception e) {
            log.error("Ошибка при отправке email на адрес {}: {}", email, e.getMessage());
            throw new RuntimeException("Не удалось отправить email", e);
        }
    }
}