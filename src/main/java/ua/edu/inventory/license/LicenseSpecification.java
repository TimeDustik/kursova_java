package ua.edu.inventory.license;

import org.springframework.data.jpa.domain.Specification;
import ua.edu.inventory.license.dto.LicenseFilterDto;

import java.util.UUID;

/**
 * Pattern: Specification.
 * Reason: той самий підхід, що й EquipmentSpecification —
 * гнучкий та тестований механізм побудови динамічних запитів.
 */
public final class LicenseSpecification {

    private LicenseSpecification() {}

    public static Specification<License> hasSiteId(UUID siteId) {
        return (root, q, cb) -> siteId == null ? cb.conjunction()
                : cb.equal(root.get("siteId"), siteId);
    }

    public static Specification<License> hasType(LicenseType type) {
        return (root, q, cb) -> type == null ? cb.conjunction()
                : cb.equal(root.get("licenseType"), type);
    }

    public static Specification<License> assignedTo(UUID assignedUserId) {
        return (root, q, cb) -> assignedUserId == null ? cb.conjunction()
                : cb.equal(root.get("assignedUserId"), assignedUserId);
    }

    public static Specification<License> searchQuery(String query) {
        return (root, q, cb) -> {
            if (query == null || query.isBlank()) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("softwareName")), like),
                    cb.like(cb.lower(root.get("vendor")), like)
            );
        };
    }

    public static Specification<License> from(LicenseFilterDto filter) {
        return Specification.where(hasSiteId(filter.siteId()))
                .and(hasType(filter.licenseType()))
                .and(assignedTo(filter.assignedUserId()))
                .and(searchQuery(filter.q()));
    }
}
