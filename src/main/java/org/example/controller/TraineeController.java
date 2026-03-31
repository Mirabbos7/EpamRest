package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.TraineeDtoRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.request.UpdateTraineeTrainersRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.TrainingType;
import org.example.service.TraineeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee", description = "Trainee management endpoints")
public class TraineeController {

    private final TraineeService traineeService;

    @Operation(summary = "Register a new Trainee")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeDtoRequest request) {
        log.info("POST /api/trainees/register firstName={}", request.firstName());
        RegistrationResponse response = traineeService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Trainee profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeResponse> getProfile(
            @PathVariable String username) {
        log.info("GET /api/trainees/{}", username);
        return traineeService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update Trainee profile")
    @PutMapping("/{username}")
    public ResponseEntity<TraineeResponse> update(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        log.info("PUT /api/trainees/{}", username);
        TraineeResponse response = traineeService.update(username, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Trainee by username")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(
            @PathVariable String username) {
        log.info("DELETE /api/trainees/{}", username);
        traineeService.delete(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get active trainers not assigned to trainee")
    @GetMapping("/{username}/trainers/unassigned")
    public ResponseEntity<List<TrainerShortResponse>> getUnassignedTrainers(
            @PathVariable String username) {
        log.info("GET /api/trainees/{}/trainers/unassigned", username);
        List<TrainerShortResponse> trainers = traineeService.getUnassignedTrainers(username);
        return ResponseEntity.ok(trainers);
    }

    @Operation(summary = "Update Trainee's trainer list")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeResponse> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        log.info("PUT /api/trainees/{}/trainers", username);
        TraineeResponse response = traineeService.updateTrainers(username, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainings for a Trainee")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingType.TrainingTypeName trainingType) {
        log.info("GET /api/trainees/{}/trainings", username);
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }
        List<TrainingResponse> trainings = traineeService.getTrainings(
                username, fromDate, toDate, trainerName, trainingType);
        return ResponseEntity.ok(trainings);
    }

    @Operation(summary = "Activate or deactivate Trainee")
    @PatchMapping("/{username}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestParam boolean isActive) {
        log.info("PATCH /api/trainees/{}/active isActive={}", username, isActive);
        traineeService.setActive(username, isActive);
        return ResponseEntity.noContent().build();
    }
}