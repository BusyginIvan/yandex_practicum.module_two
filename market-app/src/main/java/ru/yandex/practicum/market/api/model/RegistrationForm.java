package ru.yandex.practicum.market.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must contain 3 to 50 characters")
    String username,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must contain 6 to 100 characters")
    String password
) { }
