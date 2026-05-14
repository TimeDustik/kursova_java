package ua.edu.inventory.site;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import ua.edu.inventory.site.dto.SiteCreateDto;
import ua.edu.inventory.site.dto.SiteDto;
import ua.edu.inventory.site.dto.SiteUpdateDto;
import ua.edu.inventory.user.UserRepository;
import ua.edu.inventory.user.UserRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final SiteMapper siteMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** ADMIN/AUDITOR бачать всі сайти; TEAM_LEAD — лише свій. */
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public List<SiteDto> getAll() {
        UserPrincipal p = SecurityUtils.getCurrentPrincipal().orElseThrow();
        if ("TEAM_LEAD".equals(p.getRole()) && p.getSiteId() != null) {
            return siteRepository.findById(p.getSiteId())
                    .map(siteMapper::toDto).map(List::of).orElse(List.of());
        }
        return siteRepository.findAll().stream().map(siteMapper::toDto).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or " +
                  "(hasRole('TEAM_LEAD') and hasPermission(#id, 'Site', 'READ'))")
    public SiteDto getById(UUID id) {
        return siteMapper.toDto(findOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SiteDto create(SiteCreateDto dto) {
        if (dto.teamLeadId() != null) validateTeamLead(dto.teamLeadId(), null);
        Site site = siteMapper.toEntity(dto);
        Site saved = siteRepository.save(site);
        publishEvent(AuditAction.CREATE, saved.getId().toString(), Map.of("name", saved.getName()));
        return siteMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SiteDto update(UUID id, SiteUpdateDto dto) {
        Site site = findOrThrow(id);
        if (dto.teamLeadId() != null) validateTeamLead(dto.teamLeadId(), id);
        siteMapper.updateEntity(dto, site);
        Site saved = siteRepository.save(site);
        publishEvent(AuditAction.UPDATE, saved.getId().toString(), Map.of("name", saved.getName()));
        return siteMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID id) {
        Site site = findOrThrow(id);
        siteRepository.delete(site);
        publishEvent(AuditAction.DELETE, id.toString(), Map.of("name", site.getName()));
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private Site findOrThrow(UUID id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", id));
    }

    private void validateTeamLead(UUID teamLeadId, UUID currentSiteId) {
        var user = userRepository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач", teamLeadId));
        if (user.getRole() != UserRole.TEAM_LEAD) {
            throw new BusinessRuleException("Користувач " + teamLeadId + " не є TEAM_LEAD");
        }
        siteRepository.findByTeamLeadId(teamLeadId).ifPresent(existing -> {
            if (!existing.getId().equals(currentSiteId)) {
                throw new BusinessRuleException(
                        "TEAM_LEAD вже закріплений за іншим Site: " + existing.getName());
            }
        });
    }

    private void publishEvent(AuditAction action, String entityId, Map<String, Object> payload) {
        UUID actorId = SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null);
        eventPublisher.publishEvent(
                new EntityChangedEvent(this, action, EntityType.SITE, entityId, payload, actorId, null));
    }
}
