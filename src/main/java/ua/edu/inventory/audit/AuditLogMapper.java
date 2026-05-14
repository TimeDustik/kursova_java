package ua.edu.inventory.audit;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ua.edu.inventory.audit.dto.AuditLogDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {
    AuditLogDto toDto(AuditLog auditLog);
}
