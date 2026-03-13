package org.example.service;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TraineeService {

    RegistrationResponse create(TraineeDtoRequest request);

    boolean matchUsernameAndPassword(String username, String password);

    Optional<TraineeResponse> findByUsername(String username, String password);

    void changePassword(ChangePasswordRequest request);

    TraineeResponse update(String username, String password, UpdateTraineeRequest request);

    void setActive(String username, String password, boolean active);

    void delete(String username, String password);

    List<TrainingResponse> getTrainings(String username,
                                        String password,
                                        Date fromDate,
                                        Date toDate,
                                        String trainerName,
                                        TrainingType.TrainingTypeName trainingTypeName);

    List<TrainerShortResponse> getUnassignedTrainers(String username, String password);

    TraineeResponse updateTrainers(String username, String password,
                                   UpdateTraineeTrainersRequest request);
}