package ua.edu.inventory.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.inventory.audit.AuditAction;
import ua.edu.inventory.audit.EntityChangedEvent;
import ua.edu.inventory.common.EntityType;
import ua.edu.inventory.common.SecurityUtils;
import ua.edu.inventory.common.exception.BusinessRuleException;
import ua.edu.inventory.common.exception.ConflictException;
import ua.edu.inventory.common.exception.ResourceNotFoundException;
import ua.edu.inventory.site.SiteRepository;
import ua.edu.inventory.user.dto.UserCreateDto;
import ua.edu.inventory.user.dto.UserDto;
import ua.edu.inventory.user.dto.UserUpdateDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Сервіс управління користувачами. CRUD тільки для ADMIN. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#siteId, 'Site', 'READ')) or hasRole('AUDITOR')")
    public List<UserDto> getBySite(UUID siteId) {
        return userRepository.findAllBySiteIdAndActiveTrue(siteId).stream()
                .map(userMapper::toDto).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or " +
                  "(hasRole('TEAM_LEAD') and hasPermission(#id, 'User', 'READ')) or " +
                  "#id == authentication.principal.id")
    public UserDto getById(UUID id) {
        return userMapper.toDto(findOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto create(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new ConflictException("Користувач з логіном '" + dto.username() + "' вже існує");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Користувач з email '" + dto.email() + "' вже існує");
        }
        validateSiteForRole(dto.role(), dto.siteId());

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        User saved = userRepository.save(user);

        publishEvent(AuditAction.CREATE, saved.getId().toString(),
                Map.of("username", saved.getUsername(), "role", saved.getRole()));
        log.info("Створено користувача: {} (роль={})", saved.getUsername(), saved.getRole());
        return userMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto update(UUID id, UserUpdateDto dto) {
        User user = findOrThrow(id);
        if (dto.email() != null && !dto.email().equals(user.getEmail())
                && userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email '" + dto.email() + "' вже використовується");
        }
        userMapper.updateEntity(dto, user);
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        User saved = userRepository.save(user);
        publishEvent(AuditAction.UPDATE, saved.getId().toString(),
                Map.of("username", saved.getUsername()));
        return userMapper.toDto(saved);
    }

    /** М'яке видалення (active=false). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deactivate(UUID id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
        publishEvent(AuditAction.DELETE, id.toString(), Map.of("username", user.getUsername()));
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач", id));
    }

    private void validateSiteForRole(UserRole role, UUID siteId) {
        if ((role == UserRole.WORKER || role == UserRole.TEAM_LEAD) && siteId == null) {
            throw new BusinessRuleException("WORKER та TEAM_LEAD повинні бути прив'язані до Site");
        }
        if (siteId != null && !siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site", siteId);
        }
    }

    private void publishEvent(AuditAction action, String entityId, Map<String, Object> payload) {
        UUID actorId = SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null);
        eventPublisher.publishEvent(
                new EntityChangedEvent(this, action, EntityType.USER, entityId, payload, actorId, null));
    }
}
