package org.example.mapper;

import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "name",             source = "trainingName")
    @Mapping(target = "date",             source = "trainingDate")
    @Mapping(target = "durationInMinutes",source = "durationMinutes")
    @Mapping(target = "trainer",          ignore = true)
    @Mapping(target = "trainee",          ignore = true)
    @Mapping(target = "trainingType",     ignore = true)
    Training toEntity(TrainingDtoRequest request);

    @Mapping(target = "trainingName",  source = "name")
    @Mapping(target = "trainingDate",  source = "date")
    @Mapping(target = "trainingType",  source = "trainingType.trainingTypeName")
    @Mapping(target = "durationMinutes", source = "durationInMinutes")
    @Mapping(target = "trainerName",   source = "trainer.user.username")
    @Mapping(target = "traineeName",   ignore = true)
    TrainingResponse toTraineeTrainingResponse(Training training);

    @Mapping(target = "trainingName",  source = "name")
    @Mapping(target = "trainingDate",  source = "date")
    @Mapping(target = "trainingType",  source = "trainingType.trainingTypeName")
    @Mapping(target = "durationMinutes", source = "durationInMinutes")
    @Mapping(target = "traineeName",   source = "trainee.user.username")
    @Mapping(target = "trainerName",   ignore = true)
    TrainingResponse toTrainerTrainingResponse(Training training);
}