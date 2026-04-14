package org.example.mapper;

import org.example.dto.request.TrainerWorkloadRequest;
import org.example.entity.Training;
import org.example.enums.ActionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingWorkloadMapper {

    @Mapping(target = "trainerUsername",  source = "training.trainer.user.username")
    @Mapping(target = "trainerFirstName", source = "training.trainer.user.firstName")
    @Mapping(target = "trainerLastName",  source = "training.trainer.user.lastName")
    @Mapping(target = "isActive",         source = "training.trainer.user.active")
    @Mapping(target = "trainingDate",     source = "training.date")
    @Mapping(target = "trainingDuration", source = "training.durationInMinutes")
    @Mapping(target = "actionType",       source = "action")
    TrainerWorkloadRequest toWorkloadRequest(Training training, ActionType action);
}