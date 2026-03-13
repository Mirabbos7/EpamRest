package org.example.dto.response;

import java.util.Date;

public record TrainingResponse(
        String trainingName,
        Date trainingDate,
        String trainingType,
        int durationMinutes,
        String trainerName,
        String traineeName
) {}
