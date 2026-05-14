package ua.edu.inventory.license.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.edu.inventory.license.LicenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LicenseCreateDto(
        @NotBlank(message = "Назва ПЗ обов'язкова") String softwareName,
        String vendor,
        @NotBlank(message = "Ліцензійний ключ обов'язковий") String licenseKey,
        @NotNull(message = "Тип ліцензії обов'язковий") LicenseType licenseType,
        @Min(value = 1, message = "Кількість місць мінімум 1") int seatsTotal,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        BigDecimal cost,
        @NotNull(message = "Об'єкт (siteId) обов'язковий") UUID siteId,
        String notes
) {}
