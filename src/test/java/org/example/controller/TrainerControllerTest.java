package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.TrainingType;
import org.example.security.service.JwtTokenService;
import org.example.service.TrainerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String USERNAME = "john.doe";

    @BeforeEach
    void setupSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(
                USERNAME, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldReturn200_withRegistrationResponse() throws Exception {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "John", "Doe", TrainingType.TrainingTypeName.CARDIO);
        RegistrationResponse response = new RegistrationResponse("john.doe", "pass123", "jwt.token");

        when(trainerService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("john.doe")))
                .andExpect(jsonPath("$.password", is("pass123")))
                .andExpect(jsonPath("$.token", is("jwt.token")));

        verify(trainerService).create(any());
    }

    @Test
    void register_shouldReturn400_whenFirstNameMissing() throws Exception {
        TrainerDtoRequest request = new TrainerDtoRequest(
                null, "Doe", TrainingType.TrainingTypeName.CARDIO);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void register_shouldReturn400_whenLastNameMissing() throws Exception {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "John", null, TrainingType.TrainingTypeName.CARDIO);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getProfile_shouldReturn200_whenFound() throws Exception {
        TrainerResponse response = new TrainerResponse(
                USERNAME, "John", "Doe", TrainingType.TrainingTypeName.CARDIO, true, List.of());

        when(trainerService.findByUsername(USERNAME)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/trainers/{username}", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(USERNAME)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.specialization", is("CARDIO")));

        verify(trainerService).findByUsername(USERNAME);
    }

    @Test
    void getProfile_shouldReturn404_whenNotFound() throws Exception {
        when(trainerService.findByUsername(USERNAME)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainers/{username}", USERNAME))
                .andExpect(status().isNotFound());

        verify(trainerService).findByUsername(USERNAME);
    }

    @Test
    void update_shouldReturn200_withUpdatedResponse() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                USERNAME, "John", "Doe", TrainingType.TrainingTypeName.CARDIO, true);
        TrainerResponse response = new TrainerResponse(
                USERNAME, "John", "Doe", TrainingType.TrainingTypeName.CARDIO, true, List.of());

        when(trainerService.update(eq(USERNAME), any())).thenReturn(response);

        mockMvc.perform(put("/api/trainers")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(USERNAME)))
                .andExpect(jsonPath("$.isActive", is(true)));

        verify(trainerService).update(eq(USERNAME), any());
    }

    @Test
    void update_shouldReturn400_whenBodyInvalid() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                null, null, null, null, true);

        mockMvc.perform(put("/api/trainers")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainings_shouldReturn200_withList() throws Exception {
        TrainingResponse training = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, null, "jane.doe");

        when(trainerService.getTrainings(eq(USERNAME), isNull(), isNull(), isNull()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName", is("Morning Run")))
                .andExpect(jsonPath("$[0].durationMinutes", is(60)));

        verify(trainerService).getTrainings(eq(USERNAME), isNull(), isNull(), isNull());
    }

    @Test
    void getTrainings_shouldReturn200_withEmptyList() throws Exception {
        when(trainerService.getTrainings(eq(USERNAME), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        verify(trainerService).getTrainings(eq(USERNAME), isNull(), isNull(), isNull());
    }

    @Test
    void setActive_shouldReturn200_whenActivated() throws Exception {
        doNothing().when(trainerService).setActive(USERNAME, true);

        mockMvc.perform(patch("/api/trainers/active")
                        .param("username", USERNAME)
                        .param("isActive", "true"))
                .andExpect(status().isOk());

        verify(trainerService).setActive(USERNAME, true);
    }

    @Test
    void setActive_shouldReturn200_whenDeactivated() throws Exception {
        doNothing().when(trainerService).setActive(USERNAME, false);

        mockMvc.perform(patch("/api/trainers/active")
                        .param("username", USERNAME)
                        .param("isActive", "false"))
                .andExpect(status().isOk());

        verify(trainerService).setActive(USERNAME, false);
    }

    @Test
    void setActive_shouldReturn400_whenParamsMissing() throws Exception {
        mockMvc.perform(patch("/api/trainers/active"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void changePassword_shouldReturn200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "oldPass", "newPass");

        doNothing().when(trainerService).changePassword(any());

        mockMvc.perform(put("/api/trainers/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword(any());
    }

    @Test
    void changePassword_shouldReturn400_whenBodyInvalid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(null, null, null);

        mockMvc.perform(put("/api/trainers/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }
}