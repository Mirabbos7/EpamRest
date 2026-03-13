package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotEmpty(message = "Trainers list must not be empty")
        List<String> trainerUsernames
) {}