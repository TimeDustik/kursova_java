package ua.edu.inventory.audit;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

/**
 * Pattern: Specification — динамічні фільтри для журналу аудиту.
 */
public final class AuditLogSpecification {

    private AuditLogSpecification() {}

    public static Specification<AuditLog> hasActor(UUID actorUserId) {
        return (root, q, cb) -> actorUserId == null ? cb.conjunction()
                : cb.equal(root.get("actorUserId"), actorUserId);
    }

    public static Specification<AuditLog> hasEntityType(String entityType) {
        return (root, q, cb) -> entityType == null || entityType.isBlank() ? cb.conjunction()
                : cb.equal(root.get("entityType"), entityType);
    }

    public static Specification<AuditLog> hasEntityId(String entityId) {
        return (root, q, cb) -> entityId == null || entityId.isBlank() ? cb.conjunction()
                : cb.equal(root.get("entityId"), entityId);
    }

    public static Specification<AuditLog> afterDate(LocalDateTime from) {
        return (root, q, cb) -> from == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<AuditLog> beforeDate(LocalDateTime to) {
        return (root, q, cb) -> to == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<AuditLog> hasAction(AuditAction action) {
        return (root, q, cb) -> action == null ? cb.conjunction()
                : cb.equal(root.get("action"), action);
    }

    /** TEAM_LEAD: лише записи де актором є хтось із його Site. */
    public static Specification<AuditLog> actorIn(Collection<UUID> actorIds) {
        return (root, q, cb) -> actorIds == null || actorIds.isEmpty() ? cb.disjunction()
                : root.get("actorUserId").in(actorIds);
    }
}
