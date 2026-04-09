package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.dto.response.TrainingTypeResponse;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.mapper.TrainingMapper;
import org.example.mapper.TrainingTypeMapper;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.example.service.TrainingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingTypeMapper trainingTypeMapper;

    @Override
    @Transactional
    public TrainingResponse create(TrainingDtoRequest request) {
        Trainer trainer = trainerRepository
                .findByUserUsername(request.trainerUsername())
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + request.trainerUsername()));

        Trainee trainee = traineeRepository
                .findByUserUsername(request.traineeUsername())
                .orElseThrow(() -> new RuntimeException("Trainee not found: " + request.traineeUsername()));

        TrainingType type = trainingTypeRepository
                .findByTrainingTypeName(request.typeName())
                .orElseThrow(() -> new RuntimeException("TrainingType not found: " + request.typeName()));

        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(type);
        training.setName(request.trainingName());
        training.setDate(request.trainingDate());
        training.setDurationInMinutes(request.durationMinutes());

        trainingRepository.save(training);
        log.info("Created training: name={}, trainee={}, trainer={}",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
        return trainingMapper.toTraineeTrainingResponse(training);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingResponse> select(Long id) {
        return trainingRepository.findById(id)
                .map(trainingMapper::toTraineeTrainingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTraineeTrainings(String traineeUsername,
                                                      Date fromDate,
                                                      Date toDate,
                                                      String trainerUsername,
                                                      TrainingType.TrainingTypeName type) {
        return trainingRepository.findByTraineeUserUsername(traineeUsername).stream()
                .filter(t -> fromDate == null || !t.getDate().before(fromDate))
                .filter(t -> toDate == null || !t.getDate().after(toDate))
                .filter(t -> trainerUsername == null ||
                        t.getTrainer().getUser().getUsername().equalsIgnoreCase(trainerUsername))
                .filter(t -> type == null ||
                        t.getTrainingType().getTrainingTypeName() == type)
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainerTrainings(String trainerUsername,
                                                      Date fromDate,
                                                      Date toDate,
                                                      String traineeUsername) {
        return trainingRepository.findByTrainerUserUsername(trainerUsername).stream()
                .filter(t -> fromDate == null || !t.getDate().before(fromDate))
                .filter(t -> toDate == null || !t.getDate().after(toDate))
                .filter(t -> traineeUsername == null ||
                        t.getTrainee().getUser().getUsername().equalsIgnoreCase(traineeUsername))
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainingsForTraineesNextWeek(List<Long> traineeIds) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date nextWeek = cal.getTime();

        return trainingRepository.findAll().stream()
                .filter(t -> traineeIds.contains(t.getTrainee().getId()))
                .filter(t -> !t.getDate().before(now) && !t.getDate().after(nextWeek))
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingTypeRepository.findAll()
                .stream()
                .map(trainingTypeMapper::toResponse)
                .toList();
    }
}