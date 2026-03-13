package org.example.dto.request;

import org.example.entity.TrainingType;

import javax.validation.constraints.NotNull;

public record UpdateTrainerRequest(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        TrainingType.TrainingTypeName specialization,
        @NotNull
        boolean isActive
) {}
