package org.example.dto.request;

import javax.validation.constraints.NotBlank;

public record UserLoginDtoRequest(@NotBlank String username, @NotBlank String password) {
}
