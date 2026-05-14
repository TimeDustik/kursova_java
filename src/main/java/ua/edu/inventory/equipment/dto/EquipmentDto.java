package ua.edu.inventory.equipment.dto;

import ua.edu.inventory.equipment.EquipmentStatus;
import ua.edu.inventory.equipment.EquipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EquipmentDto(
        UUID id,
        String inventoryNumber,
        String name,
        EquipmentType type,
        String manufacturer,
        String model,
        String serialNumber,
        EquipmentStatus status,
        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal price,
        UUID siteId,
        UUID assignedUserId,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
