package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
        @NotNull
        String firstName,
        String lastName,
        @NotNull
        String password
) {
}
