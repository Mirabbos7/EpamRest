package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;
import org.example.service.TraineeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Api(tags = "Trainee Management")
public class TraineeController {

    private final TraineeService traineeService;

    @PostMapping("/register")
    @ApiOperation("Trainee Registration")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody TraineeDtoRequest request) {
        return traineeService.create(request);
    }

    @GetMapping("/login")
    @ApiOperation("Trainee Login")
    public void login(@Valid @RequestBody UserLoginDtoRequest request) {
        // Тут сервис проверяет username/password
        traineeService.matchUsernameAndPassword(request.username(), request.password());
    }

    @PutMapping("/change-password")
    @ApiOperation("Change Trainee Password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        traineeService.changePassword(request);
    }

    @GetMapping("/profile")
    @ApiOperation("Get Trainee Profile")
    public TraineeResponse getProfile(@RequestParam String username, @RequestParam String password) {
        return traineeService.findByUsername(new UserLoginDtoRequest(username, password))
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
    }

    @PutMapping("/profile")
    @ApiOperation("Update Trainee Profile")
    public TraineeResponse updateProfile(@Valid @RequestBody UpdateTraineeRequest request) {
        return traineeService.update(request);
    }

    @DeleteMapping
    @ApiOperation("Delete Trainee Profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody UserLoginDtoRequest request) {
        traineeService.delete(request);
    }

    @GetMapping("/trainings")
    @ApiOperation("Get Trainee Trainings List")
    public List<TrainingResponse> getTrainings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) Date fromDate,
            @RequestParam(required = false) Date toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingType.TrainingTypeName typeName
    ) {
        return traineeService.getTrainings(username, password, fromDate, toDate, trainerName, typeName);
    }

    @GetMapping("/trainers/unassigned")
    @ApiOperation("Get Not Assigned Active Trainers")
    public List<TrainerShortResponse> getUnassignedTrainers(@Valid @RequestBody UserLoginDtoRequest request) {
        return traineeService.getUnassignedTrainers(request);
    }

    @PutMapping("/trainers")
    @ApiOperation("Update Trainee's Trainer List")
    public TraineeResponse updateTrainers(@Valid @RequestBody UpdateTraineeTrainersRequest request) {
        return traineeService.updateTrainers(request);
    }

    @PatchMapping("/activate")
    @ApiOperation("Activate/Deactivate Trainee")
    public void setActive(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam boolean active) {
        traineeService.setActive(username, password, active);
    }
}
