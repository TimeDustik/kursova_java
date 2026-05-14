package ua.edu.inventory.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.edu.inventory.equipment.EquipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentCreateDto(
        @NotBlank(message = "Назва обладнання обов'язкова") String name,
        @NotNull(message = "Тип обладнання обов'язковий") EquipmentType type,
        String manufacturer,
        String model,
        String serialNumber,
        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal price,
        @NotNull(message = "Об'єкт (siteId) обов'язковий") UUID siteId,
        String notes
) {}
