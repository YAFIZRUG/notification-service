package edu.aston.notification_service.integration;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import edu.aston.notification_service.dto.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.mail.internet.MimeMessage; 

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.mail.host=localhost",
    "spring.mail.port=3025",
    "spring.mail.username=test",
    "spring.mail.password=test",
    "spring.mail.properties.mail.smtp.auth=false"
})
@EmbeddedKafka(partitions = 1, topics = {"user.events"})
class KafkaEmailIntegrationTest {
    
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
        .withPerMethodLifecycle(false);
    
    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;
    
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("notification-service.kafka.topic.user-events", () -> "user.events");
    }
    
    @Test
    void testSendEmailOnUserCreatedEvent() throws Exception {

        UserEvent event = new UserEvent("test@example.com", UserEvent.Operation.CREATED);
        
        kafkaTemplate.send("user.events", event);
        
        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        
        assertEquals(1, receivedMessages.length);
        assertEquals("Добро пожаловать на наш сайт!", receivedMessages[0].getSubject());
        assertTrue(receivedMessages[0].getContent().toString()
            .contains("Ваш аккаунт на сайте ваш сайт был успешно создан"));
        assertEquals("test@example.com", receivedMessages[0].getAllRecipients()[0].toString());
    }
    
    @Test
    void testSendEmailOnUserDeletedEvent() throws Exception {
        // Given
        UserEvent event = new UserEvent("delete@example.com", UserEvent.Operation.DELETED);
        
        // When
        kafkaTemplate.send("user.events", event);
        
        // Then - ждем получения email
        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        
        assertEquals(1, receivedMessages.length);
        assertEquals("Ваш аккаунт был удален", receivedMessages[0].getSubject());
        assertTrue(receivedMessages[0].getContent().toString()
            .contains("Ваш аккаунт был удалён"));
        assertEquals("delete@example.com", receivedMessages[0].getAllRecipients()[0].toString());
    }
}