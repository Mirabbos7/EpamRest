package org.example.dto.request;

import javax.validation.constraints.NotNull;
import java.util.Date;

public record UpdateTraineeRequest(
        @NotNull
        String username,

        @NotNull
        String password,
        Date dateOfBirth,
        String address,
        @NotNull
        boolean isActive
) {}
