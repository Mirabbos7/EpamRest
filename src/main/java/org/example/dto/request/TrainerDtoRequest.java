package org.example.dto.request;

import org.example.entity.TrainingType;

import javax.validation.constraints.NotNull;

public record TrainerDtoRequest(@NotNull String firstName, @NotNull String lastName,
                                @NotNull TrainingType.TrainingTypeName specialization) {
}
