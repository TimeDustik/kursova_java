package ua.edu.inventory.equipment.dto;

import ua.edu.inventory.equipment.EquipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentUpdateDto(
        String name,
        String manufacturer,
        String model,
        String serialNumber,
        EquipmentStatus status,
        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal price,
        String notes
) {}
