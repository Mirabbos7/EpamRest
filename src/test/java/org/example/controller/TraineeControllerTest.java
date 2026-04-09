package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.TraineeDtoRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.request.UpdateTraineeTrainersRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.TrainingType;
import org.example.config.service.JwtTokenService;
import org.example.service.TraineeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String USERNAME = "John.Doe";

    @Test
    void register_shouldReturn200_withRegistrationResponse() throws Exception {
        TraineeDtoRequest request = new TraineeDtoRequest("John", "Doe", null, null);
        RegistrationResponse response = new RegistrationResponse("John.Doe", "pass123", "jwt.token");

        when(traineeService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.password", is("pass123")))
                .andExpect(jsonPath("$.token", is("jwt.token")));

        verify(traineeService).create(any());
    }

    @Test
    void register_shouldReturn400_whenFirstNameMissing() throws Exception {
        TraineeDtoRequest request = new TraineeDtoRequest(null, "Doe", null, null);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_shouldReturn200_whenFound() throws Exception {
        TraineeResponse response = new TraineeResponse(
                "John.Doe", "John", "Doe", null, null, true, List.of());

        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/trainees/{username}", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(traineeService).findByUsername(USERNAME);
    }

    @Test
    void getProfile_shouldReturn404_whenNotFound() throws Exception {
        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainees/{username}", USERNAME))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_withUpdatedResponse() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Doe", "John.Doe", null, "Tashkent", true);
        TraineeResponse response = new TraineeResponse(
                "John.Doe", "John", "Doe", null, "Tashkent", true, List.of());

        when(traineeService.update(eq(USERNAME), any())).thenReturn(response);

        mockMvc.perform(put("/api/trainees/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.address", is("Tashkent")));

        verify(traineeService).update(eq(USERNAME), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(traineeService).delete(USERNAME);

        mockMvc.perform(delete("/api/trainees/{username}", USERNAME))
                .andExpect(status().isNoContent());

        verify(traineeService).delete(USERNAME);
    }

    @Test
    void getUnassignedTrainers_shouldReturn200_withList() throws Exception {
        TrainerShortResponse trainer = new TrainerShortResponse(
                "jane.smith", "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/{username}/trainers/unassigned", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("jane.smith")))
                .andExpect(jsonPath("$[0].specialization", is("CARDIO")));

        verify(traineeService).getUnassignedTrainers(USERNAME);
    }

    @Test
    void updateTrainers_shouldReturn200() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Doe", List.of("jane.smith"));
        TraineeResponse response = new TraineeResponse(
                "John.Doe", "John", "Doe", null, null, true, List.of());

        when(traineeService.updateTrainers(eq(USERNAME), any())).thenReturn(response);

        mockMvc.perform(put("/api/trainees/{username}/trainers", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")));

        verify(traineeService).updateTrainers(eq(USERNAME), any());
    }

    @Test
    void getTrainings_shouldReturn200_withList() throws Exception {
        TrainingResponse training = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, "jane.smith", null);

        when(traineeService.getTrainings(eq(USERNAME), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName", is("Morning Run")))
                .andExpect(jsonPath("$[0].durationMinutes", is(60)));

        verify(traineeService).getTrainings(eq(USERNAME), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void setActive_shouldReturn204() throws Exception {
        doNothing().when(traineeService).setActive(USERNAME, false);

        mockMvc.perform(patch("/api/trainees/{username}/active", USERNAME)
                        .param("isActive", "false"))
                .andExpect(status().isNoContent());

        verify(traineeService).setActive(USERNAME, false);
    }
}