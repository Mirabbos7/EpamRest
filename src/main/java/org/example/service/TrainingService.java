package org.example.service;

import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.dto.response.TrainingTypeResponse;
import org.example.entity.TrainingType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrainingService {

    TrainingResponse create(TrainingDtoRequest request);

    Optional<TrainingResponse> select(Long id);

    List<TrainingResponse> getTraineeTrainings(String traineeUsername,
                                               Date fromDate,
                                               Date toDate,
                                               String trainerUsername,
                                               TrainingType.TrainingTypeName typeName);

    List<TrainingResponse> getTrainerTrainings(String trainerUsername,
                                               Date fromDate,
                                               Date toDate,
                                               String traineeUsername);

    List<TrainingResponse> getTrainingsForTraineesNextWeek(List<Long> traineeIds);

    List<TrainingTypeResponse> getTrainingTypes();

}