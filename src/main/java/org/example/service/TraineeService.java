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

    Optional<TraineeResponse> findByUsername(UserLoginDtoRequest request);

    void changePassword(ChangePasswordRequest request);

    TraineeResponse update(UpdateTraineeRequest request);

    void setActive(String username, String password, boolean active);

    void delete(UserLoginDtoRequest request);

    List<TrainingResponse> getTrainings(String username,
                                        String password,
                                        Date fromDate,
                                        Date toDate,
                                        String trainerName,
                                        TrainingType.TrainingTypeName trainingTypeName);

    List<TrainerShortResponse> getUnassignedTrainers(UserLoginDtoRequest request);

    TraineeResponse updateTrainers(UpdateTraineeTrainersRequest request);
}