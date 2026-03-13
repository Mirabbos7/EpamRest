package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.*;
import org.example.dto.response.*;
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

    // 1. POST /api/trainees/register
    @Operation(summary = "Register a new Trainee")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeDtoRequest request) {
        log.info("POST /api/trainees/register firstName={}", request.firstName());
        RegistrationResponse response = traineeService.create(request);
        return ResponseEntity.ok(response);
    }

    // 5. GET /api/trainees/{username}
    @Operation(summary = "Get Trainee profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        log.info("GET /api/trainees/{}", username);
        return traineeService.findByUsername(authUsername, authPassword)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. PUT /api/trainees
    @Operation(summary = "Update Trainee profile")
    @PutMapping
    public ResponseEntity<TraineeResponse> update(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @Valid @RequestBody UpdateTraineeRequest request) {
        log.info("PUT /api/trainees username={}", request.username());
        TraineeResponse response = traineeService.update(authUsername, authPassword, request);
        return ResponseEntity.ok(response);
    }

    // 7. DELETE /api/trainees/{username}
    @Operation(summary = "Delete Trainee profile")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        log.info("DELETE /api/trainees/{}", username);
        traineeService.delete(authUsername, authPassword);
        return ResponseEntity.ok().build();
    }

    // 10. GET /api/trainees/{username}/unassigned-trainers
    @Operation(summary = "Get active trainers not assigned to trainee")
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerShortResponse>> getUnassignedTrainers(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        log.info("GET /api/trainees/{}/unassigned-trainers", username);
        List<TrainerShortResponse> trainers =
                traineeService.getUnassignedTrainers(authUsername, authPassword);
        return ResponseEntity.ok(trainers);
    }

    // 11. PUT /api/trainees/trainers
    @Operation(summary = "Update Trainee's trainer list")
    @PutMapping("/trainers")
    public ResponseEntity<TraineeResponse> updateTrainers(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        log.info("PUT /api/trainees/trainers traineeUsername={}", request.username());
        TraineeResponse response =
                traineeService.updateTrainers(authUsername, authPassword, request);
        return ResponseEntity.ok(response);
    }

    // 12. GET /api/trainees/{username}/trainings
    @Operation(summary = "Get Trainee trainings list with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingType.TrainingTypeName trainingType) {
        log.info("GET /api/trainees/{}/trainings", username);
        List<TrainingResponse> trainings = traineeService.getTrainings(
                authUsername, authPassword, fromDate, toDate, trainerName, trainingType);
        return ResponseEntity.ok(trainings);
    }

    // 15. PATCH /api/trainees/active
    @Operation(summary = "Activate or deactivate Trainee")
    @PatchMapping("/active")
    public ResponseEntity<Void> setActive(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam String username,
            @RequestParam boolean isActive) {
        log.info("PATCH /api/trainees/active username={} isActive={}", username, isActive);
        traineeService.setActive(authUsername, authPassword, isActive);
        return ResponseEntity.ok().build();
    }
}