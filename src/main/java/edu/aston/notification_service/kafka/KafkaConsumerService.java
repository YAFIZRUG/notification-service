package edu.aston.notification_service.kafka;

import edu.aston.notification_service.dto.UserEvent;
import edu.aston.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EmailService emailService;

    @Value("${notification-service.kafka.topic.user-events:user.events}")
    private String userEventsTopic;

    @KafkaListener(topics = "${notification-service.kafka.topic.user-events:user.events}", groupId = "${spring.kafka.consumer.group-id:notification-group}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserEvent(UserEvent userEvent) {
        log.info("Получено событие из Kafka: email={}, operation={}",
                userEvent.getEmail(), userEvent.getOperation());

        try {
            emailService.sendNotificationEmail(
                    userEvent.getEmail(),
                    userEvent.getOperation().name());

            log.info("Обработка события для {} завершена успешно", userEvent.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при обработке события для {}: {}",
                    userEvent.getEmail(), e.getMessage());
        }
    }
}