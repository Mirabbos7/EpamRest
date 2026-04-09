package org.example.dto.response;

import org.example.entity.TrainingType;

public record TrainerShortResponse(
        String username,
        String firstName,
        String lastName,
        TrainingType.TrainingTypeName specialization
) {
}
