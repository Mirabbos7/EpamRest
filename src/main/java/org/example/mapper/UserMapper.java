package org.example.mapper;

import org.example.dto.response.RegistrationResponse;
import org.example.entity.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface UserMapper {

    RegistrationResponse toRegistrationResponse(User user);
}