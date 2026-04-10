package org.example.dto.request;

import lombok.Builder;
import lombok.Data;
import org.example.enums.ActionType;

import java.util.Date;

@Data
@Builder
public class TrainerWorkloadRequest {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private Date trainingDate;
    private int trainingDuration;
    private ActionType actionType;
}
