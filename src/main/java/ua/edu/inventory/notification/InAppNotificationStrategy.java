package ua.edu.inventory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.edu.inventory.notification.NotificationRepository;
import ua.edu.inventory.notification.NotificationStatus;
import ua.edu.inventory.notification.NotificationStrategy;

import java.time.LocalDateTime;

/**
 * Pattern: Strategy — конкретна стратегія «в застосунку».
 * Reason: зберігає сповіщення у БД зі статусом SENT;
 * Thymeleaf-UI може їх відобразити без зовнішніх сервісів.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationStrategy implements NotificationStrategy {

    private final NotificationRepository notificationRepository;

    @Override
    public void send(Notification notification) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("InApp-сповіщення збережено: type={} recipient={}",
                notification.getType(), notification.getRecipientUserId());
    }

    @Override
    public boolean supports(NotificationType type) {
        return true; // підтримує всі типи
    }
}
