package edu.aston.notification_service.integration;

import edu.aston.notification_service.service.EmailService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=false"
})
class EmailServiceTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(false);

    @Autowired
    private EmailService emailService;

    @Test
    void testSendCreatedNotification() throws Exception {

        emailService.sendNotificationEmail("test@example.com", "CREATED");

        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertEquals(1, receivedMessages.length);
        assertEquals("Добро пожаловать на наш сайт!", receivedMessages[0].getSubject());
        String content = receivedMessages[0].getContent().toString();
        assertTrue(content.contains("Ваш аккаунт на сайте ваш сайт был успешно создан"));
    }

    @Test
    void testSendDeletedNotification() throws Exception {

        emailService.sendNotificationEmail("delete@example.com", "DELETED");

        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertEquals(1, receivedMessages.length);
        assertEquals("Ваш аккаунт был удален", receivedMessages[0].getSubject());
        String content = receivedMessages[0].getContent().toString();
        assertTrue(content.contains("Ваш аккаунт был удалён"));
    }

    @Test
    void testUnknownOperationDoesNotSendEmail() {

        emailService.sendNotificationEmail("test@example.com", "UNKNOWN");

        assertEquals(0, greenMail.getReceivedMessages().length);
    }
}