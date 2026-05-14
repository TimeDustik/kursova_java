package ua.edu.inventory.license;

import jakarta.persistence.*;
import lombok.*;
import ua.edu.inventory.common.BaseAuditableEntity;
import ua.edu.inventory.crypto.LicenseKeyAttributeConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Сутність програмної ліцензії.
 * licenseKey зберігається в БД у зашифрованому вигляді (AES/GCM).
 *
 * Pattern: Decorator застосовано через LicenseKeyAttributeConverter,
 * який прозоро шифрує/дешифрує поле.
 *
 * Бізнес-правила:
 * - seatsUsed <= seatsTotal
 * - expirationDate = null для PERPETUAL ліцензій
 * - assignedUserId.siteId == license.siteId
 */
@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "software_name", nullable = false)
    private String softwareName;

    private String vendor;

    /**
     * Pattern: Decorator — LicenseKeyAttributeConverter шифрує значення
     * перед збереженням і дешифрує при читанні з БД.
     */
    @Column(name = "license_key", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = LicenseKeyAttributeConverter.class)
    private String licenseKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, columnDefinition = "license_type")
    private LicenseType licenseType;

    @Column(name = "seats_total", nullable = false)
    @Builder.Default
    private int seatsTotal = 1;

    @Column(name = "seats_used", nullable = false)
    @Builder.Default
    private int seatsUsed = 0;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /** null для PERPETUAL */
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean hasAvailableSeats() {
        return seatsUsed < seatsTotal;
    }

    public void incrementSeats() {
        if (!hasAvailableSeats()) {
            throw new IllegalStateException("Всі місця ліцензії вже використані");
        }
        seatsUsed++;
    }

    public void decrementSeats() {
        if (seatsUsed <= 0) {
            throw new IllegalStateException("Кількість використаних місць вже 0");
        }
        seatsUsed--;
    }
}
