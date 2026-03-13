package org.example.mapper;

import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TraineeShortResponse;
import org.example.entity.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TrainerMapper.class)
public interface TraineeMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "trainers", target = "trainers")
    TraineeResponse toResponse(Trainee trainee);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TraineeShortResponse toShortResponse(Trainee trainee);
}
