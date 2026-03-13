package org.example.dto.response;

import java.util.List;

public record TrainerResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        boolean isActive,
        List<TraineeShortResponse> trainees
) {}
