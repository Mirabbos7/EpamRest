package org.example.service;

import org.example.dto.response.UserCreateResult;

public interface UserService {

    UserCreateResult createUser(String firstName, String lastName);
}