package org.example.dto.request;

import org.example.entity.TrainingType;

import java.util.Date;

public record TrainingDtoRequest(
        String traineeUsername,
        String trainerUsername,
        String trainingName,
        TrainingType.TrainingTypeName typeName,
        Date trainingDate,
        int durationMinutes
) {}