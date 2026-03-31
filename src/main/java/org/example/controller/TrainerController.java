package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainingResponse;
import org.example.service.TrainerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
        log.info("POST /api/trainers/register firstName={}, lastName={}",
                request.firstName(), request.lastName());
        RegistrationResponse response = trainerService.create(request);
        log.info("Trainer registered successfully: username={}", response.username());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Trainer profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerResponse> getProfile(@PathVariable String username) {
        log.info("GET /api/trainers/{}", username);
        return trainerService.findByUsername(username)
                .map(response -> {
                    log.info("Trainer profile found: username={}", username);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("Trainer not found: username={}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Update Trainer profile")
    @PutMapping
    public ResponseEntity<TrainerResponse> update(
            @Valid @RequestBody UpdateTrainerRequest request) {
        String username = getCurrentUsername();
        log.info("PUT /api/trainers username={}", username);
        TrainerResponse response = trainerService.update(username, request);
        log.info("Trainer updated successfully: username={}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Trainer trainings list with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) String traineeName) {
        log.info("GET /api/trainers/{}/trainings fromDate={}, toDate={}, traineeName={}",
                username, fromDate, toDate, traineeName);
        List<TrainingResponse> trainings = trainerService.getTrainings(
                username, fromDate, toDate, traineeName);
        log.info("Returning {} trainings for trainer: username={}", trainings.size(), username);
        return ResponseEntity.ok(trainings);
    }

    @Operation(summary = "Activate or deactivate Trainer")
    @PatchMapping("/active")
    public ResponseEntity<Void> setActive(
            @RequestParam String username,
            @RequestParam boolean isActive) {
        log.info("PATCH /api/trainers/active username={}, isActive={}", username, isActive);
        trainerService.setActive(username, isActive);
        log.info("Trainer active status updated: username={}, isActive={}", username, isActive);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change Trainer password")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("PUT /api/trainers/change-password username={}", request.username());
        trainerService.changePassword(request);
        log.info("Password changed successfully for trainer: username={}", request.username());
        return ResponseEntity.ok().build();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}