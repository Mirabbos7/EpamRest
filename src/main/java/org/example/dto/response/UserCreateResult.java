package org.example.dto.response;

import org.example.entity.User;

public record UserCreateResult(User user, String rawPassword) {
}
