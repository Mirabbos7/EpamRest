package org.example.dto.request;

import org.example.entity.TrainingType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record TrainerDtoRequest(@NotBlank String firstName, @NotBlank String lastName,
                                @NotNull TrainingType.TrainingTypeName specialization) {
}
