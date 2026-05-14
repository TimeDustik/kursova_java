package ua.edu.inventory.site.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SiteCreateDto(
        @NotBlank(message = "Назва об'єкту обов'язкова") String name,
        String address,
        String city,
        UUID teamLeadId
) {}
