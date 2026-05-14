package ua.edu.inventory.license;

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
import ua.edu.inventory.common.exception.ResourceNotFoundException;
import ua.edu.inventory.license.dto.LicenseCreateDto;
import ua.edu.inventory.license.dto.LicenseDto;
import ua.edu.inventory.license.dto.LicenseFilterDto;
import ua.edu.inventory.license.dto.LicenseUpdateDto;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final LicenseMapper licenseMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize("isAuthenticated()")
    public Page<LicenseDto> getAll(LicenseFilterDto filter, Pageable pageable) {
        UserPrincipal p = SecurityUtils.getCurrentPrincipal().orElseThrow();
        Specification<License> spec = LicenseSpecification.from(filter);

        if ("TEAM_LEAD".equals(p.getRole())) {
            spec = spec.and(LicenseSpecification.hasSiteId(p.getSiteId()));
        } else if ("WORKER".equals(p.getRole())) {
            spec = spec.and(LicenseSpecification.assignedTo(p.getId()));
        }
        return licenseRepository.findAll(spec, pageable).map(licenseMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or hasPermission(#id, 'License', 'READ')")
    public LicenseDto getById(UUID id) {
        return licenseMapper.toDto(findOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'License', 'READ')")
    public List<LicenseDto> getByUser(UUID userId) {
        return licenseRepository.findAllByAssignedUserId(userId).stream()
                .map(licenseMapper::toDto).toList();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#dto.siteId, 'Site', 'READ'))")
    @Transactional
    public LicenseDto create(LicenseCreateDto dto) {
        if (dto.licenseType() != LicenseType.PERPETUAL && dto.expirationDate() == null) {
            throw new BusinessRuleException("Для не-PERPETUAL ліцензій дата закінчення обов'язкова");
        }
        License license = licenseMapper.toEntity(dto);
        License saved = licenseRepository.save(license);
        publishEvent(AuditAction.CREATE, saved.getId().toString(),
                Map.of("softwareName", saved.getSoftwareName()));
        return licenseMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'License', 'WRITE'))")
    @Transactional
    public LicenseDto update(UUID id, LicenseUpdateDto dto) {
        License license = findOrThrow(id);
        licenseMapper.updateEntity(dto, license);
        License saved = licenseRepository.save(license);
        publishEvent(AuditAction.UPDATE, id.toString(), Map.of("softwareName", saved.getSoftwareName()));
        return licenseMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'License', 'WRITE'))")
    @Transactional
    public void delete(UUID id) {
        License license = findOrThrow(id);
        licenseRepository.delete(license);
        publishEvent(AuditAction.DELETE, id.toString(), Map.of("softwareName", license.getSoftwareName()));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'License', 'MANAGE'))")
    @Transactional
    public LicenseDto assign(UUID id, UUID userId) {
        License license = findOrThrow(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач", userId));

        if (!license.getSiteId().equals(user.getSiteId())) {
            throw new BusinessRuleException("Користувач належить до іншого об'єкту");
        }
        if (!license.hasAvailableSeats()) {
            throw new BusinessRuleException("Всі місця ліцензії вже використані (" +
                    license.getSeatsUsed() + "/" + license.getSeatsTotal() + ")");
        }
        license.incrementSeats();
        license.setAssignedUserId(userId);
        License saved = licenseRepository.save(license);
        publishEvent(AuditAction.ASSIGN, id.toString(),
                Map.of("assignedUserId", userId.toString(), "username", user.getUsername()));
        return licenseMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#id, 'License', 'MANAGE'))")
    @Transactional
    public LicenseDto unassign(UUID id) {
        License license = findOrThrow(id);
        license.decrementSeats();
        license.setAssignedUserId(null);
        License saved = licenseRepository.save(license);
        publishEvent(AuditAction.UPDATE, id.toString(), Map.of("unassigned", true));
        return licenseMapper.toDto(saved);
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private License findOrThrow(UUID id) {
        return licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ліцензія", id));
    }

    private void publishEvent(AuditAction action, String entityId, Map<String, Object> payload) {
        UUID actorId = SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null);
        eventPublisher.publishEvent(
                new EntityChangedEvent(this, action, EntityType.LICENSE, entityId, payload, actorId, null));
    }
}
