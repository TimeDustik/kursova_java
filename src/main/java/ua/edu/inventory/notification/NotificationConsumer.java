package ua.edu.inventory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Слухач черги сповіщень.
 * Отримує NotificationMessage з RabbitMQ, створює Notification-сутність
 * і передає до NotificationService для обробки через стратегії.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queues.notification}")
    public void consume(NotificationMessage message) {
        log.info("Отримано сповіщення з черги: type={} recipient={}",
                message.type(), message.recipientUserId());

        Notification notification = Notification.builder()
                .recipientUserId(message.recipientUserId())
                .type(message.type())
                .payload(message.payload())
                .status(NotificationStatus.PENDING)
                .build();

        notificationService.process(notification);
    }
}
