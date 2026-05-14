package ua.edu.inventory.auth.dto;

import ua.edu.inventory.user.UserRole;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        String phone,
        UserRole role,
        UUID siteId,
        boolean active
) {}
