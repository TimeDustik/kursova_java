package ua.edu.inventory.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token не може бути порожнім") String refreshToken
) {}
