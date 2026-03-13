package org.example.mapper;

import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TraineeMapper.class)
public interface TrainerMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "trainingType.trainingTypeName", target = "specialization")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "trainees", target = "trainees") // uses TraineeMapper.toShortResponse
    TrainerResponse toResponse(Trainer trainer);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "trainingType.trainingTypeName", target = "specialization")
    TrainerShortResponse toShortResponse(Trainer trainer);
}
