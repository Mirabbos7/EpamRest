package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.service.TrainerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer", description = "Trainer management endpoints")
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Register a new Trainer")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerDtoRequest request) {
        log.info("POST /api/trainers/register firstName={}", request.firstName());
        return ResponseEntity.ok(trainerService.create(request));
    }

    @Operation(summary = "Get Trainer profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword) {
        log.info("GET /api/trainers/{}", username);
        return trainerService.findByUsername(authUsername, authPassword)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update Trainer profile")
    @PutMapping
    public ResponseEntity<TrainerResponse> update(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @Valid @RequestBody UpdateTrainerRequest request) {
        log.info("PUT /api/trainers username={}", request.username());
        return ResponseEntity.ok(trainerService.update(authUsername, authPassword, request));
    }

    @Operation(summary = "Get Trainer trainings list with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String traineeName) {
        log.info("GET /api/trainers/{}/trainings", username);
        return ResponseEntity.ok(
                trainerService.getTrainings(authUsername, authPassword, fromDate, toDate, traineeName)
        );
    }

    @Operation(summary = "Activate or deactivate Trainer")
    @PatchMapping("/active")
    public ResponseEntity<Void> setActive(
            @RequestHeader("username") String authUsername,
            @RequestHeader("password") String authPassword,
            @RequestParam String username,
            @RequestParam boolean isActive) {
        log.info("PATCH /api/trainers/active username={} isActive={}", username, isActive);
        trainerService.setActive(authUsername, authPassword, isActive);
        return ResponseEntity.ok().build();
    }
}