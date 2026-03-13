package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotBlank
        String username,

        @NotBlank
        String oldPassword,

        @NotBlank
        String newPassword
) {
}
