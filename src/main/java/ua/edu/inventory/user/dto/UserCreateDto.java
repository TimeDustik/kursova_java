package ua.edu.inventory.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ua.edu.inventory.user.UserRole;

import java.util.UUID;

public record UserCreateDto(
        @NotBlank(message = "Логін обов'язковий")
        @Size(min = 3, max = 100, message = "Логін від 3 до 100 символів")
        String username,

        @NotBlank(message = "Email обов'язковий")
        @Email(message = "Невалідний формат email")
        String email,

        @NotBlank(message = "Пароль обов'язковий")
        @Size(min = 8, message = "Пароль мінімум 8 символів")
        String password,

        String fullName,
        String phone,

        @NotNull(message = "Роль обов'язкова")
        UserRole role,

        UUID siteId
) {}
