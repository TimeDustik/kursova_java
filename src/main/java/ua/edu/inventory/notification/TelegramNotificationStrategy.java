package ua.edu.inventory.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pattern: Strategy — конкретна стратегія «Telegram».
 * Reason: заглушка для розширення системи в майбутньому без зміни коду
 * NotificationService (принцип Open/Closed — SOLID).
 * Наразі не підтримує жодного типу (supports=false).
 */
@Component
@Slf4j
public class TelegramNotificationStrategy implements NotificationStrategy {

    @Override
    public void send(Notification notification) {
        log.info("[TELEGRAM STUB] Bot API — type={} recipient={}",
                notification.getType(), notification.getRecipientUserId());
    }

    @Override
    public boolean supports(NotificationType type) {
        return false; // вимкнено до підключення реального Telegram Bot API
    }
}
