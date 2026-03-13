package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.entity.*;
import org.example.mapper.TrainingMapper;
import org.example.repository.*;
import org.example.service.TrainingService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingMapper trainingMapper;

    @Override
    public TrainingResponse create(TrainingDtoRequest request) {

        Trainer trainer = trainerRepository
                .findByUserUsername(request.trainerUsername())
                .orElseThrow();

        Trainee trainee = traineeRepository
                .findByUserUsername(request.traineeUsername())
                .orElseThrow();

        TrainingType type = new TrainingType();
        type.setTrainingTypeName(request.typeName());

        Training training = new Training();

        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(type);
        training.setName(request.trainingName());
        training.setDate(request.trainingDate());
        training.setDurationInMinutes(request.durationMinutes());

        trainingRepository.save(training);

        return trainingMapper.toTraineeTrainingResponse(training);
    }

    @Override
    public Optional<TrainingResponse> select(Long id) {

        return trainingRepository.findById(id)
                .map(trainingMapper::toTraineeTrainingResponse);
    }

    @Override
    public List<TrainingResponse> getTraineeTrainings(String traineeUsername,
                                                      Date fromDate,
                                                      Date toDate,
                                                      String trainerUsername,
                                                      TrainingType.TrainingTypeName type) {

        return trainingRepository.findAll()
                .stream()
                .filter(t -> t.getTrainee().getUser().getUsername().equals(traineeUsername))
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    public List<TrainingResponse> getTrainerTrainings(String trainerUsername,
                                                      Date fromDate,
                                                      Date toDate,
                                                      String traineeUsername) {

        return trainingRepository.findAll()
                .stream()
                .filter(t -> t.getTrainer().getUser().getUsername().equals(trainerUsername))
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    public List<TrainingResponse> getTrainingsForTraineesNextWeek(List<Long> traineeIds) {

        return trainingRepository.findAll()
                .stream()
                .filter(t -> traineeIds.contains(t.getTrainee().getId()))
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }
}