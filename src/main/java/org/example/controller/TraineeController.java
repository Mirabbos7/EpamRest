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
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        // TODO:
        //  [Optional]
        //  Given that most of your endpoints expect same custom username header, can you find a way to log API calls in more
        //  centralized manner? That kind of solution should take HTTP verbs and paths dynamically.
        log.info("GET /api/trainees/{}", username);
        return traineeService.findByUsername(authUsername, authPassword)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(
            @PathVariable String username,
            @RequestHeader("password") String authPassword) {
        traineeService.delete(username, authPassword);
        return ResponseEntity.ok().build();
    }

    // TODO:
    //  [Optional]
    //  Can be more RESTful endpoint path: /{username}/trainers/unassigned
    @Operation(summary = "Get active trainers not assigned to trainee")
    @GetMapping("/{username}/trainers/unassigned")
    public ResponseEntity<List<TrainerShortResponse>> getUnassignedTrainers(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        log.info("GET /api/trainees/{}/unassigned-trainers", username);
        List<TrainerShortResponse> trainers =
                traineeService.getUnassignedTrainers(authUsername, authPassword);
        return ResponseEntity.ok(trainers);
    }

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

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingType.TrainingTypeName trainingType) {

        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }

        List<TrainingResponse> trainings = traineeService.getTrainings(
                authUsername, authPassword, fromDate, toDate, trainerName, trainingType);
        return ResponseEntity.ok(trainings);
    }

    @Operation(summary = "Activate or deactivate Trainee")
    @PatchMapping("/active")
    public ResponseEntity<Void> setActive(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam String username,
            @RequestParam boolean isActive) {
        // TODO:
        //  [Optional]
        //  Your approach to pass an explicit flag is totally fine and I would even say it is cleaner and safer.
        //  However, if we stick to the task 'Activate/De-activate Trainee/Trainer profile not idempotent action',
        //  it is more expected to have a toggle action without an isActive param which does smth like active=!active
        log.info("PATCH /api/trainees/active username={} isActive={}", username, isActive);
        traineeService.setActive(authUsername, authPassword, isActive);
        return ResponseEntity.ok().build();
    }
}