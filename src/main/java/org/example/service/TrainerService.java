package org.example.service;

import org.example.dto.request.*;
import org.example.dto.response.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrainerService {

    RegistrationResponse create(TrainerDtoRequest request);

    boolean matchUsernameAndPassword(String username, String password);

    Optional<TrainerResponse> findByUsername(String username, String password);

    void changePassword(ChangePasswordRequest request);

    TrainerResponse update(String username, String password, UpdateTrainerRequest updateTraineeRequest);

    void setActive(String username, String password, boolean active);

    List<TrainingResponse> getTrainings(String username,
                                        String password,
                                        Date fromDate,
                                        Date toDate,
                                        String traineeName);
}