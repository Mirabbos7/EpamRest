package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.entity.TrainingType;

public record TrainerDtoRequest(@NotBlank String firstName, @NotBlank String lastName,
                                @NotNull TrainingType.TrainingTypeName specialization) {
}
