package ua.edu.inventory.site.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SiteDto(
        UUID id,
        String name,
        String address,
        String city,
        UUID teamLeadId,
        LocalDateTime createdAt
) {}
