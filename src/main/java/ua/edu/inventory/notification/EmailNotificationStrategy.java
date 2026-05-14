package ua.edu.inventory.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pattern: Strategy — конкретна стратегія «Email».
 * Reason: заглушка (stub) для демонстрації патерну.
 * У продакшн-версії тут буде JavaMailSender / SendGrid / AWS SES.
 * Підтримує: LICENSE_EXPIRING та WARRANTY_EXPIRING.
 */
@Component
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public void send(Notification notification) {
        // Stub: у реальній системі — відправка через SMTP/API
        log.info("[EMAIL STUB] Відправка email: type={} recipient={} payload={}",
                notification.getType(),
                notification.getRecipientUserId(),
                notification.getPayload());
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.LICENSE_EXPIRING
            || type == NotificationType.WARRANTY_EXPIRING;
    }
}
