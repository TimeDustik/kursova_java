package ua.edu.inventory.auth.dto;

import ua.edu.inventory.user.UserRole;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String username,
            String fullName,
            String email,
            UserRole role,
            UUID siteId
    ) {}
}
