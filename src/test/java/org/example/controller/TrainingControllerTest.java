package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingTypeResponse;
import org.example.entity.TrainingType;
import org.example.mapper.TrainingTypeMapper;
import org.example.repository.TrainingTypeRepository;
import org.example.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;

    @MockitoBean
    private TrainingTypeMapper trainingTypeMapper;

    @Test
    void addTraining_shouldReturn200() throws Exception {
        TrainingDtoRequest request = new TrainingDtoRequest(
                "john.doe", "jane.smith", "Morning Run", TrainingType.TrainingTypeName.CARDIO ,new Date(), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("username", "john.doe")
                        .header("password", "pass123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainingService).create(any());
    }

    @Test
    void addTraining_shouldReturn400_whenBodyInvalid() throws Exception {
        TrainingDtoRequest request = new TrainingDtoRequest(
                null, null, null, null, new Date(), 0);

        mockMvc.perform(post("/api/trainings")
                        .header("username", "john.doe")
                        .header("password", "pass123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainingTypes_shouldReturn200_withList() throws Exception {
        TrainingType type1 = new TrainingType();
        TrainingType type2 = new TrainingType();

        TrainingTypeResponse response1 = new TrainingTypeResponse(1L, "CARDIO");
        TrainingTypeResponse response2 = new TrainingTypeResponse(2L, "STRENGTH");

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type1, type2));
        when(trainingTypeMapper.toResponse(type1)).thenReturn(response1);
        when(trainingTypeMapper.toResponse(type2)).thenReturn(response2);

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingType", is("CARDIO")))
                .andExpect(jsonPath("$[1].trainingType", is("STRENGTH")));

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeMapper, times(2)).toResponse(any());
    }

    @Test
    void getTrainingTypes_shouldReturn200_withEmptyList() throws Exception {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        verify(trainingTypeRepository).findAll();
    }
}