package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.service.JwtTokenService;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingTypeResponse;
import org.example.entity.TrainingType;
import org.example.service.TrainingService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void addTraining_shouldReturn200() throws Exception {
        TrainingDtoRequest request = new TrainingDtoRequest(
                "john.doe", "jane.smith", "Morning Run",
                TrainingType.TrainingTypeName.CARDIO, new Date(), 60);

        mockMvc.perform(post("/api/trainings")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainingTypes_shouldReturn200_withList() throws Exception {
        when(trainingService.getTrainingTypes()).thenReturn(
                List.of(new TrainingTypeResponse(1L, "CARDIO"),
                        new TrainingTypeResponse(2L, "STRENGTH")));

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingType", is("CARDIO")))
                .andExpect(jsonPath("$[1].trainingType", is("STRENGTH")));

        verify(trainingService).getTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturn200_withEmptyList() throws Exception {
        when(trainingService.getTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        verify(trainingService).getTrainingTypes();
    }
}