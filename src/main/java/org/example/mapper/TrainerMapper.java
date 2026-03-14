package org.example.mapper;

import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    @Mapping(target = "trainees",     ignore = true)
    @Mapping(target = "trainings",    ignore = true)
    Trainer toEntity(TrainerDtoRequest request);

    @Mapping(target = "username",       source = "user.username")
    @Mapping(target = "firstName",      source = "user.firstName")
    @Mapping(target = "lastName",       source = "user.lastName")
    @Mapping(target = "specialization", source = "trainingType.trainingTypeName")
    @Mapping(target = "isActive",       source = "user.active")
    @Mapping(target = "trainees",       source = "trainees")
    TrainerResponse toResponse(Trainer trainer);

    @Mapping(target = "username",       source = "user.username")
    @Mapping(target = "firstName",      source = "user.firstName")
    @Mapping(target = "lastName",       source = "user.lastName")
    @Mapping(target = "specialization", source = "trainingType.trainingTypeName")
    TrainerShortResponse toShortResponse(Trainer trainer);

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "user.username",  ignore = true)
    @Mapping(target = "user.password",  ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName",  source = "lastName")
    @Mapping(target = "user.active",    source = "isActive")
    @Mapping(target = "trainingType",   ignore = true)
    @Mapping(target = "trainees",       ignore = true)
    @Mapping(target = "trainings",      ignore = true)
    void updateEntity(UpdateTrainerRequest request, @MappingTarget Trainer trainer);
}