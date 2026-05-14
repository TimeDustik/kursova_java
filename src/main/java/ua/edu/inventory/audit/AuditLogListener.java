package ua.edu.inventory.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pattern: Observer.
 * Reason: Spring ApplicationEventPublisher (Observable) публікує EntityChangedEvent,
 * а цей listener (@EventListener) автоматично реагує та записує аудит-лог.
 * Сервіси не мають прямої залежності від AuditLogRepository —
 * вся логіка аудиту зосереджена тут.
 *
 * @Transactional(REQUIRES_NEW): аудит зберігається у окремій транзакції,
 * щоб помилка слухача не відкочувала основну бізнес-транзакцію.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntityChanged(EntityChangedEvent event) {
        AuditLog log = AuditLog.builder()
                .actorUserId(event.getActorUserId())
                .action(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .payload(event.getPayload())
                .ipAddress(event.getIpAddress())
                .build();
        auditLogRepository.save(log);
        logger.debug("Аудит: {} {} id={} actor={}",
                event.getAction(), event.getEntityType(),
                event.getEntityId(), event.getActorUserId());
    }

    // Slf4j генерує поле log, а не logger — використаємо явне
    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(AuditLogListener.class);
}
