package ua.edu.inventory.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findAllByActorUserId(UUID actorUserId, Pageable pageable);

    Page<AuditLog> findAllByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);
}
