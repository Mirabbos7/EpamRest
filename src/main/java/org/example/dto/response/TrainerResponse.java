package org.example.dto.response;

import org.example.entity.TrainingType;

import java.util.List;

public record TrainerResponse(
        String username,
        String firstName,
        String lastName,
        TrainingType.TrainingTypeName specialization,
        boolean isActive,
        List<TraineeShortResponse> trainees
) {}
