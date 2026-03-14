package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDtoRequest(@NotBlank String username, @NotBlank String password) {
}
