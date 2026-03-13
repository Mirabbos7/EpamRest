package org.example.mapper;

import org.example.dto.response.TrainingResponse;
import org.example.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(source = "name", target = "trainingName")
    @Mapping(source = "date", target = "trainingDate")
    @Mapping(source = "durationInMinutes", target = "durationMinutes")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainer.user.username", target = "trainerName")
    @Mapping(source = "trainee.user.username", target = "traineeName")
    TrainingResponse toResponse(Training training);
}