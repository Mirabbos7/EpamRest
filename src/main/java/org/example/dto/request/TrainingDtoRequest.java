package org.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.entity.TrainingType;

import java.util.Date;

public record TrainingDtoRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotNull TrainingType.TrainingTypeName typeName,
        @NotNull Date trainingDate,
        @Min(1) int durationMinutes
) {
}