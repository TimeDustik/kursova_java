package ua.edu.inventory.license.dto;

import ua.edu.inventory.license.LicenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LicenseDto(
        UUID id,
        String softwareName,
        String vendor,
        String licenseKey,
        LicenseType licenseType,
        int seatsTotal,
        int seatsUsed,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        BigDecimal cost,
        UUID siteId,
        UUID assignedUserId,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
