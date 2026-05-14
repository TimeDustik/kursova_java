package ua.edu.inventory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Публікує повідомлення-сповіщення у RabbitMQ-чергу.
 * NotificationConsumer слухає та обробляє їх асинхронно.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.inventory}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.notification}")
    private String routingKey;

    public void send(UUID recipientUserId, NotificationType type, Map<String, Object> payload) {
        NotificationMessage message = NotificationMessage.of(recipientUserId, type, payload);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.debug("Повідомлення надіслано до RabbitMQ: type={} recipient={}", type, recipientUserId);
    }

    public void sendLicenseExpiry(UUID recipientUserId, String softwareName,
                                   String expirationDate, UUID licenseId) {
        send(recipientUserId, NotificationType.LICENSE_EXPIRING, Map.of(
                "licenseId",      licenseId.toString(),
                "softwareName",   softwareName,
                "expirationDate", expirationDate,
                "message",        "Ліцензія «" + softwareName + "» спливає " + expirationDate
        ));
    }

    public void sendWarrantyExpiry(UUID recipientUserId, String equipmentName,
                                    String warrantyUntil, UUID equipmentId) {
        send(recipientUserId, NotificationType.WARRANTY_EXPIRING, Map.of(
                "equipmentId",   equipmentId.toString(),
                "equipmentName", equipmentName,
                "warrantyUntil", warrantyUntil,
                "message",       "Гарантія «" + equipmentName + "» закінчується " + warrantyUntil
        ));
    }

    public void sendAssetAssigned(UUID recipientUserId, String equipmentName, UUID equipmentId) {
        send(recipientUserId, NotificationType.ASSET_ASSIGNED, Map.of(
                "equipmentId",   equipmentId.toString(),
                "equipmentName", equipmentName,
                "message",       "Вам призначено обладнання: «" + equipmentName + "»"
        ));
    }
}
