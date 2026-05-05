package org.example.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TraineeDtoRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @Past
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        @Size(max = 256)
        String address

) {
}