package org.example.mapper;

import org.example.dto.request.TraineeDtoRequest;
import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(TraineeDtoRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.active")
    @Mapping(target = "trainers", source = "trainers")
    TraineeResponse toResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "trainingType.trainingTypeName")
    TrainerShortResponse trainerToShortResponse(Trainer trainer);

}