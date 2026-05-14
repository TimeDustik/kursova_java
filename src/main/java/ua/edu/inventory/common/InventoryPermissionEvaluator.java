package ua.edu.inventory.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.license.LicenseRepository;
import ua.edu.inventory.site.SiteRepository;
import ua.edu.inventory.user.UserRepository;

import java.io.Serializable;
import java.util.UUID;

/**
 * Pattern: Strategy — обирає стратегію перевірки прав залежно від ролі користувача
 * та типу сутності. Замінює hardcoded перевірки в контролерах.
 *
 * Реєструється через MethodSecurityConfig як основний PermissionEvaluator.
 *
 * Матриця прав:
 * - ADMIN      → дозволено все
 * - AUDITOR    → лише READ
 * - TEAM_LEAD  → READ/WRITE/MANAGE для сутностей свого Site
 * - WORKER     → не проходить hasPermission (обмеження у @PreAuthorize)
 *
 * Використання в сервісах:
 *   @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Equipment', 'READ')")
 *   @PreAuthorize("hasRole('ADMIN') or (hasRole('TEAM_LEAD') and hasPermission(#siteId, 'Site', 'READ'))")
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryPermissionEvaluator implements PermissionEvaluator {

    private final EquipmentRepository equipmentRepository;
    private final LicenseRepository   licenseRepository;
    private final SiteRepository      siteRepository;
    private final UserRepository      userRepository;

    /**
     * Перевірка прав по доменному об'єкту (targetDomainObject передано безпосередньо).
     * В даному проекті використовується рідко — основний метод нижче.
     */
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (targetDomainObject == null) return false;
        return isAdminOrAuditorAllowed(auth, permission);
    }

    /**
     * Перевірка прав по ідентифікатору сутності та її типу.
     *
     * @param targetId   UUID сутності або UUID сайту (для перевірки 'Site' ACCESS)
     * @param targetType EntityType: "Equipment" | "License" | "Site" | "User" | "AuditLog"
     * @param permission Permission: "READ" | "WRITE" | "MANAGE"
     */
    @Override
    public boolean hasPermission(Authentication auth,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (auth == null || targetId == null) return false;

        UserPrincipal principal = extractPrincipal(auth);
        if (principal == null) return false;

        String role = principal.getRole();

        // ADMIN має повний доступ
        if ("ADMIN".equals(role)) return true;

        // AUDITOR може тільки читати
        if ("AUDITOR".equals(role)) return Permission.READ.equals(permission);

        // TEAM_LEAD: перевіряємо чи сутність належить до його Site
        if ("TEAM_LEAD".equals(role)) {
            UUID userSiteId = principal.getSiteId();
            if (userSiteId == null) return false;

            UUID entitySiteId = resolveEntitySiteId(targetType, toUuid(targetId));
            if (entitySiteId == null) {
                log.debug("Не вдалося знайти siteId для {} з id={}", targetType, targetId);
                return false;
            }
            return userSiteId.equals(entitySiteId);
        }

        // WORKER не проходить hasPermission — обмежується через @PreAuthorize
        return false;
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    /**
     * Повертає siteId, пов'язаний із сутністю.
     * Для Site — повертає id самого сайту (перевіряємо чи user.siteId == site.id).
     */
    private UUID resolveEntitySiteId(String targetType, UUID id) {
        return switch (targetType) {
            case EntityType.EQUIPMENT ->
                equipmentRepository.findById(id).map(e -> e.getSiteId()).orElse(null);
            case EntityType.LICENSE ->
                licenseRepository.findById(id).map(l -> l.getSiteId()).orElse(null);
            case EntityType.SITE ->
                siteRepository.existsById(id) ? id : null;
            case EntityType.USER ->
                userRepository.findById(id).map(u -> u.getSiteId()).orElse(null);
            default -> {
                log.warn("Невідомий тип сутності для перевірки прав: {}", targetType);
                yield null;
            }
        };
    }

    private boolean isAdminOrAuditorAllowed(Authentication auth, Object permission) {
        UserPrincipal p = extractPrincipal(auth);
        if (p == null) return false;
        return "ADMIN".equals(p.getRole()) ||
               ("AUDITOR".equals(p.getRole()) && Permission.READ.equals(permission));
    }

    private UserPrincipal extractPrincipal(Authentication auth) {
        if (auth.getPrincipal() instanceof UserPrincipal p) return p;
        return null;
    }

    private UUID toUuid(Serializable id) {
        if (id instanceof UUID u) return u;
        if (id instanceof String s) return UUID.fromString(s);
        throw new IllegalArgumentException("Неможливо конвертувати в UUID: " + id);
    }
}
