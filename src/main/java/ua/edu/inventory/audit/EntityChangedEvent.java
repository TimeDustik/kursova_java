package ua.edu.inventory.audit;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Pattern: Observer.
 * Reason: сервіси публікують цю подію через ApplicationEventPublisher.
 * AuditLogListener (@EventListener) автоматично записує аудит-лог,
 * не створюючи прямої залежності між сервісами та аудитом.
 */
@Getter
public class EntityChangedEvent extends ApplicationEvent {

    private final AuditAction action;
    private final String entityType;
    private final String entityId;
    private final Map<String, Object> payload;
    private final UUID actorUserId;
    private final String ipAddress;

    public EntityChangedEvent(Object source, AuditAction action, String entityType,
                              String entityId, Map<String, Object> payload,
                              UUID actorUserId, String ipAddress) {
        super(source);
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.payload = payload;
        this.actorUserId = actorUserId;
        this.ipAddress = ipAddress;
    }
}
