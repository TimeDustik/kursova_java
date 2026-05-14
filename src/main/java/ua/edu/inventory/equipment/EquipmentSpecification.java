package ua.edu.inventory.equipment;

import org.springframework.data.jpa.domain.Specification;
import ua.edu.inventory.equipment.dto.EquipmentFilterDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pattern: Specification (варіант Builder).
 * Reason: забезпечує динамічну побудову JPA-запитів без SQL-рядків;
 * кожна специфікація — одна умова, що може комбінуватися через and()/or().
 * Замінює N окремих методів репозиторію на один гнучкий запит.
 */
public final class EquipmentSpecification {

    private EquipmentSpecification() {}

    public static Specification<Equipment> hasSiteId(UUID siteId) {
        return (root, q, cb) -> siteId == null ? cb.conjunction()
                : cb.equal(root.get("siteId"), siteId);
    }

    public static Specification<Equipment> hasStatus(EquipmentStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Equipment> hasType(EquipmentType type) {
        return (root, q, cb) -> type == null ? cb.conjunction()
                : cb.equal(root.get("type"), type);
    }

    public static Specification<Equipment> assignedTo(UUID assignedUserId) {
        return (root, q, cb) -> assignedUserId == null ? cb.conjunction()
                : cb.equal(root.get("assignedUserId"), assignedUserId);
    }

    /** Пошук по назві, моделі, виробнику або інвентарному номеру (case-insensitive). */
    public static Specification<Equipment> searchQuery(String query) {
        return (root, q, cb) -> {
            if (query == null || query.isBlank()) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("model")), like),
                    cb.like(cb.lower(root.get("manufacturer")), like),
                    cb.like(cb.lower(root.get("inventoryNumber")), like),
                    cb.like(cb.lower(root.get("serialNumber")), like)
            );
        };
    }

    /** Збирає всі умови з EquipmentFilterDto у єдину специфікацію. */
    public static Specification<Equipment> from(EquipmentFilterDto filter) {
        List<Specification<Equipment>> specs = new ArrayList<>();
        if (filter.siteId() != null)         specs.add(hasSiteId(filter.siteId()));
        if (filter.status() != null)         specs.add(hasStatus(filter.status()));
        if (filter.type() != null)           specs.add(hasType(filter.type()));
        if (filter.assignedUserId() != null) specs.add(assignedTo(filter.assignedUserId()));
        if (filter.q() != null)              specs.add(searchQuery(filter.q()));

        return specs.stream()
                .reduce(Specification.where(null), Specification::and);
    }
}
