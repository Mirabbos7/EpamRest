package org.example.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.example.entity.TrainingType;

import java.time.LocalDate;

public record TrainingDtoRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotNull TrainingType.TrainingTypeName typeName,
        @NotNull
        @Past
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate trainingDate,
        @Min(1) int durationMinutes
) {
}