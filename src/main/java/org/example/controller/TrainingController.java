package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingTypeResponse;
import org.example.mapper.TrainingTypeMapper;
import org.example.repository.TrainingTypeRepository;
import org.example.service.TrainingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingTypeMapper trainingTypeMapper;

    @Operation(summary = "Add a new training session")
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @Valid @RequestBody TrainingDtoRequest request) {
        log.info("POST /api/trainings trainee={} trainer={}",
                request.traineeUsername(), request.trainerUsername());
        trainingService.create(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        log.info("GET /api/trainings/types");
        List<TrainingTypeResponse> types = trainingTypeRepository.findAll()
                .stream()
                .map(trainingTypeMapper::toResponse)
                .toList();
        return ResponseEntity.ok(types);
    }
}