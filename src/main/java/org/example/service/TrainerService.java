package org.example.service;

import org.example.dto.request.*;
import org.example.dto.response.*;

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