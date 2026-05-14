package ua.edu.inventory.equipment.dto;

import ua.edu.inventory.equipment.EquipmentStatus;
import ua.edu.inventory.equipment.EquipmentType;

import java.util.UUID;

/** Параметри динамічного пошуку обладнання (JPA Specification). */
public record EquipmentFilterDto(
        UUID siteId,
        EquipmentStatus status,
        EquipmentType type,
        UUID assignedUserId,
        String q
) {}
