package org.example.service;

import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainingResponse;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrainerService {

    RegistrationResponse create(TrainerDtoRequest request);

    boolean matchUsernameAndPassword(String username, String password);

    Optional<TrainerResponse> findByUsername(String username);

    void changePassword(ChangePasswordRequest request);

    TrainerResponse update(String username, UpdateTrainerRequest request);

    void setActive(String username, boolean active);

    List<TrainingResponse> getTrainings(String username, Date fromDate, Date toDate, String traineeName);

}