package org.example.dto.request;

import javax.validation.constraints.NotNull;
import java.util.Date;

public record TraineeDtoRequest(

        @NotNull
        String firstName,

        @NotNull
        String lastName,

        Date dateOfBirth,

        String address

) {
}
