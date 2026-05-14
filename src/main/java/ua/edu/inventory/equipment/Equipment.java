package ua.edu.inventory.equipment;

import jakarta.persistence.*;
import lombok.*;
import ua.edu.inventory.common.BaseAuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Сутність одиниці обладнання.
 * inventoryNumber генерується у сервісі за шаблоном EQ-{YYYY}-{seq}.
 *
 * Бізнес-правила:
 * - status=ASSIGNED → assignedUserId обов'язковий
 * - status=IN_STOCK або DECOMMISSIONED → assignedUserId = null
 * - assignedUserId.siteId повинен збігатись з equipment.siteId
 */
@Entity
@Table(
    name = "equipment",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_equipment_inventory_number", columnNames = "inventory_number"),
        @UniqueConstraint(name = "uq_equipment_serial_number",    columnNames = "serial_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inventory_number", nullable = false, length = 50)
    private String inventoryNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "equipment_type")
    private EquipmentType type;

    private String manufacturer;

    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "equipment_status")
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.IN_STOCK;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
