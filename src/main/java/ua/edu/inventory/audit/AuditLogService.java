package ua.edu.inventory.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.inventory.audit.dto.AuditLogDto;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.SecurityUtils;
import ua.edu.inventory.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервіс для перегляду журналу аудиту.
 *
 * Права:
 *  - ADMIN / AUDITOR — бачать все
 *  - TEAM_LEAD       — бачать лише записи де актором є працівник їхнього Site
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public Page<AuditLogDto> getAll(UUID actorId,
                                    String entityType,
                                    String entityId,
                                    AuditAction action,
                                    LocalDateTime from,
                                    LocalDateTime to,
                                    Pageable pageable) {
        Specification<AuditLog> spec = buildSpec(actorId, entityType, entityId, action, from, to);
        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toDto);
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private Specification<AuditLog> buildSpec(UUID actorId, String entityType, String entityId,
                                               AuditAction action,
                                               LocalDateTime from, LocalDateTime to) {
        UserPrincipal p = SecurityUtils.getCurrentPrincipal().orElseThrow();

        Specification<AuditLog> spec = Specification.where(null);

        // TEAM_LEAD бачить лише записи своїх Site-колег
        if ("TEAM_LEAD".equals(p.getRole()) && p.getSiteId() != null) {
            List<UUID> siteUserIds = userRepository
                    .findAllBySiteIdAndActiveTrue(p.getSiteId()).stream()
                    .map(u -> u.getId()).toList();
            spec = AuditLogSpecification.actorIn(siteUserIds);
        }

        if (actorId != null)                         spec = spec.and(AuditLogSpecification.hasActor(actorId));
        if (entityType != null && !entityType.isBlank()) spec = spec.and(AuditLogSpecification.hasEntityType(entityType));
        if (entityId != null && !entityId.isBlank())     spec = spec.and(AuditLogSpecification.hasEntityId(entityId));
        if (action != null)                          spec = spec.and(AuditLogSpecification.hasAction(action));
        if (from != null)                            spec = spec.and(AuditLogSpecification.afterDate(from));
        if (to != null)                              spec = spec.and(AuditLogSpecification.beforeDate(to));

        return spec;
    }
}
