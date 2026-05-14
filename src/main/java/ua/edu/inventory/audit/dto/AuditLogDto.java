package ua.edu.inventory.audit.dto;

import ua.edu.inventory.audit.AuditAction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogDto(
        UUID id,
        UUID actorUserId,
        AuditAction action,
        String entityType,
        String entityId,
        Map<String, Object> payload,
        String ipAddress,
        LocalDateTime createdAt
) {}
