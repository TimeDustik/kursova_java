package ua.edu.inventory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервіс обробки сповіщень.
 *
 * Pattern: Strategy — отримує список усіх реалізацій NotificationStrategy
 * через Spring DI (List<NotificationStrategy>) і для кожного сповіщення
 * обирає всі стратегії, що підтримують його тип.
 * Новий канал = новий @Component без зміни цього класу.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final List<NotificationStrategy> strategies;
    private final NotificationRepository notificationRepository;

    /**
     * Зберігає нове сповіщення та відправляє через всі відповідні стратегії.
     */
    @Transactional
    public void process(Notification notification) {
        Notification saved = notificationRepository.save(notification);

        List<NotificationStrategy> matched = strategies.stream()
                .filter(s -> s.supports(saved.getType()))
                .toList();

        if (matched.isEmpty()) {
            log.warn("Не знайдено стратегії для типу сповіщення: {}", saved.getType());
            return;
        }

        matched.forEach(strategy -> {
            try {
                strategy.send(saved);
            } catch (Exception e) {
                log.error("Помилка стратегії {}: {}", strategy.getClass().getSimpleName(), e.getMessage());
                saved.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(saved);
            }
        });
    }
}
