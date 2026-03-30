package org.example.service;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TraineeService {

    RegistrationResponse create(TraineeDtoRequest request);

    Optional<TraineeResponse> findByUsername(String username);

    void changePassword(ChangePasswordRequest request);

    TraineeResponse update(String username, UpdateTraineeRequest request);

    void setActive(String username, boolean active);

    void delete(String username);

    List<TrainingResponse> getTrainings(String username,
                                        Date fromDate,
                                        Date toDate,
                                        String trainerName,
                                        TrainingType.TrainingTypeName trainingTypeName);

    List<TrainerShortResponse> getUnassignedTrainers(String username);

    TraineeResponse updateTrainers(String username, UpdateTraineeTrainersRequest request);
}