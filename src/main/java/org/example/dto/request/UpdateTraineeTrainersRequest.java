package org.example.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotNull
        String username,
        @NotNull
        String password,

        @NotNull
        List<String> trainerUsernames
) {}
