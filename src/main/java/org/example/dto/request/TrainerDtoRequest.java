package org.example.dto.request;

import org.example.entity.TrainingType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerDtoRequest(@NotBlank String firstName, @NotBlank String lastName,
                                @NotNull TrainingType.TrainingTypeName specialization) {
}
