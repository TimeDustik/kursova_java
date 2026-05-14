package ua.edu.inventory.notification;

import java.util.Map;
import java.util.UUID;

/**
 * DTO, що серіалізується у JSON і відправляється в RabbitMQ-чергу.
 * Pattern: Builder — використовується статичний фабричний метод для зручної побудови.
 */
public record NotificationMessage(
        UUID recipientUserId,
        NotificationType type,
        Map<String, Object> payload
) {
    public static NotificationMessage of(UUID recipientUserId,
                                         NotificationType type,
                                         Map<String, Object> payload) {
        return new NotificationMessage(recipientUserId, type, payload);
    }
}
