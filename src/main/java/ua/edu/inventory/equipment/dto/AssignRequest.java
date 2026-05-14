package ua.edu.inventory.equipment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRequest(@NotNull(message = "userId обов'язковий") UUID userId) {}
