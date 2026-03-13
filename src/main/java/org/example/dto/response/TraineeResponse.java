package org.example.dto.response;

import java.util.Date;
import java.util.List;

public record TraineeResponse(
        String username,
        String firstName,
        String lastName,
        Date dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerShortResponse> trainers
) {}
