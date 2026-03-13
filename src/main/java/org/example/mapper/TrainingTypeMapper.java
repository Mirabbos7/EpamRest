package org.example.mapper;

import org.example.dto.response.TrainingTypeResponse;
import org.example.entity.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    @Mapping(target = "trainingType", source = "trainingTypeName")
    TrainingTypeResponse toDto(TrainingType entity);
}
