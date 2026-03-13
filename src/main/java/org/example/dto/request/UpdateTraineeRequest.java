package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public record UpdateTraineeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank()
        String username,
        Date dateOfBirth,
        String address,
        @NotNull
        boolean isActive
) {}
