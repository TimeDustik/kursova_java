package ua.edu.inventory.license.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LicenseUpdateDto(
        String softwareName,
        String vendor,
        String licenseKey,
        Integer seatsTotal,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        BigDecimal cost,
        String notes
) {}
