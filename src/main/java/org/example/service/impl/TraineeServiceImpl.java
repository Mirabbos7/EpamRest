package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.service.AuthService;
import org.example.service.TraineeService;
import org.example.service.UserService;
import org.example.specification.TrainingSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserService userService;
    private final AuthService authService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @Override
    @Transactional
    public RegistrationResponse create(TraineeDtoRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());

        Trainee trainee = traineeMapper.toEntity(request);
        trainee.setUser(user);

        traineeRepository.save(trainee);
        log.info("Registered trainee: username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return traineeRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeResponse> findByUsername(String username, String password) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);
        return traineeRepository.findByUserUsername(username)
                .map(traineeMapper::toResponse);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        authService.authenticate(request.username(), request.oldPassword(),
                traineeRepository::existsByUserUsernameAndUserPassword);

        Trainee trainee = getTraineeByUsername(request.username());
        trainee.getUser().setPassword(request.newPassword());
        traineeRepository.save(trainee);
        log.info("Password changed for trainee: username={}", request.username());
    }

    @Override
    @Transactional
    public TraineeResponse update(String username, String password, UpdateTraineeRequest request) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);

        Trainee trainee = getTraineeByUsername(request.username());

        User user = trainee.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setActive(request.isActive());

        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        return traineeMapper.toResponse(traineeRepository.save(trainee));
    }

    @Override
    @Transactional
    public void setActive(String username, String password, boolean active) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);

        Trainee trainee = getTraineeByUsername(username);
        trainee.getUser().setActive(active);
        traineeRepository.save(trainee);
        log.info("Trainee active={} for username={}", active, username);
    }

    @Override
    @Transactional
    public void delete(String username, String password) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);
        Trainee trainee = getTraineeByUsername(username);
        traineeRepository.delete(trainee);
        log.info("Deleted trainee: username={}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username,
                                               String password,
                                               Date fromDate,
                                               Date toDate,
                                               String trainerName,
                                               TrainingType.TrainingTypeName trainingTypeName) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);

        return trainingRepository
                .findAll(TrainingSpecification.byTraineeCriteria(
                        username, fromDate, toDate, trainerName, trainingTypeName))
                .stream()
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponse> getUnassignedTrainers(String username, String password) {
        authService.authenticate(username, password,
                traineeRepository::existsByUserUsernameAndUserPassword);
        Trainee trainee = getTraineeByUsername(username);
        List<Long> assignedIds = trainee.getTrainers().stream()
                .map(Trainer::getId)
                .toList();
        return trainerRepository.findAll().stream()
                .filter(trainer -> !assignedIds.contains(trainer.getId()))
                .filter(trainer -> trainer.getUser().isActive())
                .map(trainerMapper::toShortResponse)
                .toList();
    }

    @Override
    @Transactional
    public TraineeResponse updateTrainers(String authUsername,
                                          String authPassword,
                                          UpdateTraineeTrainersRequest request) {
        authService.authenticate(authUsername, authPassword,
                traineeRepository::existsByUserUsernameAndUserPassword);

        Trainee trainee = getTraineeByUsername(request.username());
        List<Trainer> trainers = trainerRepository
                .findAllByUserUsernameIn(request.trainerUsernames());
        trainee.setTrainers(trainers);

        return traineeMapper.toResponse(traineeRepository.save(trainee));
    }

    private Trainee getTraineeByUsername(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found: " + username));
    }
}