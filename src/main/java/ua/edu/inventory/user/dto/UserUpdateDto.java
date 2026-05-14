package ua.edu.inventory.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Email(message = "Невалідний формат email")
        String email,

        String fullName,
        String phone,

        @Size(min = 8, message = "Пароль мінімум 8 символів")
        String password,

        Boolean active
) {}
