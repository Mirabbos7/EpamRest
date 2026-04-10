package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingTypeResponse;
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

    @Operation(summary = "Add a new training session")
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody TrainingDtoRequest request) {
        log.info("POST /api/trainings trainee={} trainer={}",
                request.traineeUsername(), request.trainerUsername());
        trainingService.create(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingService.getTrainingTypes());
    }

    @Operation(summary = "Delete training")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id){
        trainingService.delete(id);
        return ResponseEntity.ok().build();
    }
}