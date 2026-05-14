package ua.edu.inventory.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Логін не може бути порожнім") String username,
        @NotBlank(message = "Пароль не може бути порожнім") String password
) {}
