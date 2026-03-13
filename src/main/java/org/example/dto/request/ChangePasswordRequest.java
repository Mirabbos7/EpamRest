package org.example.dto.request;

import javax.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotNull
        String username,

        @NotNull
        String oldPassword,

        @NotNull
        String newPassword
) {
}
