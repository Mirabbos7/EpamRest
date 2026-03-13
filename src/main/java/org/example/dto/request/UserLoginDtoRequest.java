package org.example.dto.request;

import javax.validation.constraints.NotNull;

public record UserLoginDtoRequest(@NotNull String username, @NotNull String password) {
}
