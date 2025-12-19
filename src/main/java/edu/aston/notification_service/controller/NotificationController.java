package edu.aston.notification_service.controller;

import edu.aston.notification_service.dto.NotificationRequest;
import edu.aston.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final EmailService emailService;
    
    /**
     * REST API для отправки уведомления на email
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Получен запрос на отправку уведомления: email={}, operation={}", 
                request.getEmail(), request.getOperation());
        
        try {
            emailService.sendNotificationEmail(request.getEmail(), request.getOperation());
            return ResponseEntity.ok("Уведомление успешно отправлено на " + request.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Ошибка при отправке уведомления: " + e.getMessage());
        }
    }
    
    /**
     * Эндпоинт для проверки здоровья сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification service is running");
    }
}