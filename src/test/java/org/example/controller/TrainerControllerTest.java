package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;
import org.example.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerService trainerService;

    @Test
    void register_shouldReturn200_withRegistrationResponse() throws Exception {
        TrainerDtoRequest request = new TrainerDtoRequest("Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);
        RegistrationResponse response = new RegistrationResponse("Jane.Smith", "pass123");

        when(trainerService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("Jane.Smith")))
                .andExpect(jsonPath("$.password", is("pass123")));

        verify(trainerService).create(any());
    }

    @Test
    void register_shouldReturn400_whenFirstNameMissing() throws Exception {
        TrainerDtoRequest request = new TrainerDtoRequest(null, "Smith", TrainingType.TrainingTypeName.CARDIO);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_shouldReturn200_whenFound() throws Exception {
        TrainerResponse response = new TrainerResponse(
                "Jane.Smith", "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO, true, List.of());

        when(trainerService.findByUsername("Jane.Smith", "pass123"))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/trainers/Jane.Smith")
                        .header("username", "Jane.Smith")
                        .header("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("Jane.Smith")))
                .andExpect(jsonPath("$.firstName", is("Jane")));

        verify(trainerService).findByUsername("Jane.Smith", "pass123");
    }

    @Test
    void getProfile_shouldReturn404_whenNotFound() throws Exception {
        when(trainerService.findByUsername("Jane.Smith", "pass123"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainers/Jane.Smith")
                        .header("username", "Jane.Smith")
                        .header("password", "pass123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_withUpdatedResponse() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "Jane", "Smith", "Jane.Smith", TrainingType.TrainingTypeName.CARDIO, true);
        TrainerResponse response = new TrainerResponse(
                "Jane.Smith", "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO, true, List.of());

        when(trainerService.update("Jane.Smith", "pass123", request)).thenReturn(response);

        mockMvc.perform(put("/api/trainers")
                        .header("username", "Jane.Smith")
                        .header("password", "pass123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("Jane.Smith")))
                .andExpect(jsonPath("$.firstName", is("Jane")));

        verify(trainerService).update(eq("Jane.Smith"), eq("pass123"), any());
    }

    @Test
    void getTrainings_shouldReturn200_withList() throws Exception {
        TrainingResponse training = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, null, "john.doe");

        when(trainerService.getTrainings(
                eq("Jane.Smith"), eq("pass123"),
                isNull(), isNull(), isNull()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainers/Jane.Smith/trainings")
                        .header("username", "Jane.Smith")
                        .header("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName", is("Morning Run")))
                .andExpect(jsonPath("$[0].durationMinutes", is(60)));

        verify(trainerService).getTrainings(
                eq("Jane.Smith"), eq("pass123"),
                isNull(), isNull(), isNull());
    }

    @Test
    void setActive_shouldReturn200() throws Exception {
        doNothing().when(trainerService).setActive("Jane.Smith", "pass123", true);

        mockMvc.perform(patch("/api/trainers/active")
                        .header("username", "Jane.Smith")
                        .header("password", "pass123")
                        .param("username", "Jane.Smith")
                        .param("isActive", "true"))
                .andExpect(status().isOk());

        verify(trainerService).setActive("Jane.Smith", "pass123", true);
    }
}