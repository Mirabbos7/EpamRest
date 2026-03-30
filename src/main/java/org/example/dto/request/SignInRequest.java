package org.example.dto.request;


import jakarta.validation.constraints.NotNull;

public record SignInRequest(
        @NotNull
        String username,
        @NotNull
        String password
) {
}
