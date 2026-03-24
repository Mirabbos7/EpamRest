package org.example.mapper;

import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "trainingType.trainingTypeName")
    @Mapping(target = "isActive", source = "user.active")
    @Mapping(target = "trainees", source = "trainees")
    TrainerResponse toResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "trainingType.trainingTypeName")
    TrainerShortResponse toShortResponse(Trainer trainer);

}