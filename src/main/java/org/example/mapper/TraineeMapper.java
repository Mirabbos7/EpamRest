package org.example.mapper;

import org.example.dto.request.TraineeDtoRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TraineeShortResponse;
import org.example.entity.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {TrainerMapper.class})
public interface TraineeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(TraineeDtoRequest request);

    @Mapping(target = "username",   source = "user.username")
    @Mapping(target = "firstName",  source = "user.firstName")
    @Mapping(target = "lastName",   source = "user.lastName")
    @Mapping(target = "isActive",   source = "user.active")
    @Mapping(target = "trainers",   source = "trainers")
    TraineeResponse toResponse(Trainee trainee);

    @Mapping(target = "username",  source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName",  source = "user.lastName")
    TraineeShortResponse toShortResponse(Trainee trainee);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "user.username",    ignore = true)
    @Mapping(target = "user.password",    ignore = true)
    @Mapping(target = "user.firstName",   source = "firstName")
    @Mapping(target = "user.lastName",    source = "lastName")
    @Mapping(target = "user.active",      source = "isActive")
    @Mapping(target = "trainers",         ignore = true)
    @Mapping(target = "trainings",        ignore = true)
    void updateEntity(UpdateTraineeRequest request, @MappingTarget Trainee trainee);
}