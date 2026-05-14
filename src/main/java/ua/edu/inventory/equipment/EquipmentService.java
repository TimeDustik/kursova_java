package ua.edu.inventory.equipment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.inventory.audit.AuditAction;
import ua.edu.inventory.audit.EntityChangedEvent;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.EntityType;
import ua.edu.inventory.common.SecurityUtils;
import ua.edu.inventory.common.exception.BusinessRuleException;
import ua.edu.inventory.common.exception.ConflictException;
import ua.edu.inventory.common.exception.ResourceNotFoundException;
import ua.edu.inventory.equipment.dto.EquipmentCreateDto;
import ua.edu.inventory.equipment.dto.EquipmentDto;
import ua.edu.inventory.equipment.dto.EquipmentFilterDto;
import ua.edu.inventory.equipment.dto.EquipmentUpdateDto;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Сервіс управління обладнанням.
 * Pattern: Factory Method — використовує EquipmentFactory для створення об'єктів.
 * Pattern: Specification — EquipmentSpecification для динамічних фільтрів.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final EquipmentMapper equipmentMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** WORKER бачить тільки своє. TEAM_LEAD — своє Site. ADMIN/AUDITOR — все. */
    @PreAuthorize("isAuthenticated()")
    public Page<EquipmentDto> getAll(EquipmentFilterDto filter, Pageable pageable) {
        UserPrincipal p = SecurityUtils.getCurrentPrincipal().orElseThrow();
        Specification<Equipment> spec = EquipmentSpecification.from(filter);

        spec = switch (p.getRole()) {
            case "TEAM_LEAD" -> spec.and(EquipmentSpecification.hasSiteId(p.getSiteId()));
            case "WORKER"    -> spec.and(EquipmentSpecification.assignedTo(p.getId()));
            default          -> spec;
        };
        return equipmentRepository.findAll(spec, pageable).map(equipmentMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or hasPermission(#id, 'Equipment', 'READ')")
    public EquipmentDto getById(UUID id) {
        return equipmentMapper.toDto(findOrThrow(id));
    }

    /** Pattern: Factory Method — EquipmentFactory.forType() створює об'єкт з дефолтами. */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#dto.siteId, 'Site', 'READ'))")
    @Transactional
    public EquipmentDto create(EquipmentCreateDto dto) {
        if (dto.serialNumber() != null && equipmentRepository.existsBySerialNumber(dto.serialNumber())) {
            throw new ConflictException("Обладнання з серійним номером '" + dto.serialNumber() + "' вже існує");
        }
        String inventoryNumber = generateInventoryNumber();
        Equipment equipment = EquipmentFactory.forType(dto.type())
                .createWithDefaults(dto, inventoryNumber);
        Equipment saved = equipmentRepository.save(equipment);

        publishEvent(AuditAction.CREATE, saved.getId().toString(),
                Map.of("inventoryNumber", saved.getInventoryNumber(), "name", saved.getName()));
        return equipmentMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'Equipment', 'WRITE'))")
    @Transactional
    public EquipmentDto update(UUID id, EquipmentUpdateDto dto) {
        Equipment eq = findOrThrow(id);
        equipmentMapper.updateEntity(dto, eq);
        Equipment saved = equipmentRepository.save(eq);
        publishEvent(AuditAction.UPDATE, id.toString(), Map.of("name", saved.getName()));
        return equipmentMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'Equipment', 'WRITE'))")
    @Transactional
    public void delete(UUID id) {
        Equipment eq = findOrThrow(id);
        equipmentRepository.delete(eq);
        publishEvent(AuditAction.DELETE, id.toString(), Map.of("name", eq.getName()));
    }

    /**
     * Призначення обладнання користувачеві.
     * Бізнес-правило: assignedUser.siteId повинен збігатися з equipment.siteId.
     */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'Equipment', 'MANAGE'))")
    @Transactional
    public EquipmentDto assign(UUID id, UUID userId) {
        Equipment eq = findOrThrow(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач", userId));

        if (!eq.getSiteId().equals(user.getSiteId())) {
            throw new BusinessRuleException(
                    "Користувач належить до іншого об'єкту. Призначення неможливе.");
        }
        if (eq.getStatus() == EquipmentStatus.DECOMMISSIONED) {
            throw new BusinessRuleException("Не можна призначити списане обладнання");
        }
        eq.setAssignedUserId(userId);
        eq.setStatus(EquipmentStatus.ASSIGNED);
        Equipment saved = equipmentRepository.save(eq);

        publishEvent(AuditAction.ASSIGN, id.toString(),
                Map.of("assignedUserId", userId.toString(), "username", user.getUsername()));
        return equipmentMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'Equipment', 'MANAGE'))")
    @Transactional
    public EquipmentDto unassign(UUID id) {
        Equipment eq = findOrThrow(id);
        eq.setAssignedUserId(null);
        eq.setStatus(EquipmentStatus.IN_STOCK);
        Equipment saved = equipmentRepository.save(eq);
        publishEvent(AuditAction.UPDATE, id.toString(), Map.of("status", "IN_STOCK", "unassigned", true));
        return equipmentMapper.toDto(saved);
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private Equipment findOrThrow(UUID id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Обладнання", id));
    }

    private String generateInventoryNumber() {
        long seq = equipmentRepository.nextSeq();
        int year = LocalDate.now().getYear();
        return String.format("EQ-%d-%05d", year, seq);
    }

    private void publishEvent(AuditAction action, String entityId, Map<String, Object> payload) {
        UUID actorId = SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null);
        eventPublisher.publishEvent(
                new EntityChangedEvent(this, action, EntityType.EQUIPMENT, entityId, payload, actorId, null));
    }
}
