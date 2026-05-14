package ua.edu.inventory.user.dto;

import ua.edu.inventory.user.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        String fullName,
        String phone,
        UserRole role,
        UUID siteId,
        boolean active,
        LocalDateTime createdAt
) {}
