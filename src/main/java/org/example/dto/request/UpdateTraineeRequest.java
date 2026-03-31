package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record UpdateTraineeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank()
        String username,
        Date dateOfBirth,
        String address,
        @NotNull
        Boolean isActive
) {
}
