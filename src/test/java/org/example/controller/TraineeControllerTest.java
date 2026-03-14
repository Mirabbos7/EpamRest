package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;
import org.example.service.TraineeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
@RequiredArgsConstructor
class TraineeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @MockBean  private TraineeService traineeService;

    @Test
    void register_shouldReturn200_withRegistrationResponse() throws Exception {
        TraineeDtoRequest request = new TraineeDtoRequest("John", "Doe", null, null);
        RegistrationResponse response = new RegistrationResponse("John.Doe", "pass123");

        when(traineeService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.password", is("pass123")));

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

        when(traineeService.findByUsername("John.Doe", "pass123"))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/trainees/John.Doe")
                        .header("username", "John.Doe")
                        .header("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(traineeService).findByUsername("John.Doe", "pass123");
    }

    @Test
    void getProfile_shouldReturn404_whenNotFound() throws Exception {
        when(traineeService.findByUsername("John.Doe", "pass123"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainees/John.Doe")
                        .header("username", "John.Doe")
                        .header("password", "pass123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_withUpdatedResponse() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Doe", "John.Doe", null, "Tashkent", true);
        TraineeResponse response = new TraineeResponse(
                "John.Doe", "John", "Doe", null, "Tashkent", true, List.of());

        when(traineeService.update("John.Doe", "pass123", request)).thenReturn(response);

        mockMvc.perform(put("/api/trainees")
                        .header("username", "John.Doe")
                        .header("password", "pass123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")))
                .andExpect(jsonPath("$.address", is("Tashkent")));

        verify(traineeService).update(eq("John.Doe"), eq("pass123"), any());
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        doNothing().when(traineeService).delete("John.Doe", "pass123");

        mockMvc.perform(delete("/api/trainees/John.Doe")
                        .header("username", "John.Doe")
                        .header("password", "pass123"))
                .andExpect(status().isOk());

        verify(traineeService).delete("John.Doe", "pass123");
    }

    @Test
    void getUnassignedTrainers_shouldReturn200_withList() throws Exception {
        TrainerShortResponse trainer = new TrainerShortResponse(
                "jane.smith", "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(traineeService.getUnassignedTrainers("John.Doe", "pass123"))
                .thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/John.Doe/unassigned-trainers")
                        .header("username", "John.Doe")
                        .header("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("jane.smith")))
                .andExpect(jsonPath("$[0].specialization", is("CARDIO")));

        verify(traineeService).getUnassignedTrainers("John.Doe", "pass123");
    }

    @Test
    void updateTrainers_shouldReturn200_withUpdatedTrainee() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Doe", List.of("jane.smith"));
        TraineeResponse response = new TraineeResponse(
                "John.Doe", "John", "Doe", null, null, true, List.of());

        when(traineeService.updateTrainers("John.Doe", "pass123", request))
                .thenReturn(response);

        mockMvc.perform(put("/api/trainees/trainers")
                        .header("username", "John.Doe")
                        .header("password", "pass123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John.Doe")));

        verify(traineeService).updateTrainers(eq("John.Doe"), eq("pass123"), any());
    }

    @Test
    void getTrainings_shouldReturn200_withList() throws Exception {
        TrainingResponse training = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, "jane.smith", null);

        when(traineeService.getTrainings(
                eq("John.Doe"), eq("pass123"),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/John.Doe/trainings")
                        .header("username", "John.Doe")
                        .header("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName", is("Morning Run")))
                .andExpect(jsonPath("$[0].durationMinutes", is(60)));

        verify(traineeService).getTrainings(
                eq("John.Doe"), eq("pass123"),
                isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void setActive_shouldReturn200() throws Exception {
        doNothing().when(traineeService).setActive("John.Doe", "pass123", false);

        mockMvc.perform(patch("/api/trainees/active")
                        .header("username", "John.Doe")
                        .header("password", "pass123")
                        .param("username", "John.Doe")
                        .param("isActive", "false"))
                .andExpect(status().isOk());

        verify(traineeService).setActive("John.Doe", "pass123", false);
    }
}