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
import org.example.metrics.TrainingMetrics;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.security.service.JwtTokenService;
import org.example.service.AuthService;
import org.example.service.TraineeService;
import org.example.service.UserService;
import org.example.specification.TrainingSpecification;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingMetrics trainingMetrics;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegistrationResponse create(TraineeDtoRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());

        Trainee trainee = traineeMapper.toEntity(request);
        trainee.setUser(user);

        traineeRepository.save(trainee);
        trainingMetrics.incrementTraineeRegistration();

        String token = jwtTokenService.generateToken(user);
        log.info("Registered trainee: username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword(), token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeResponse> findByUsername(String username) {
        log.info("Fetching trainee profile: username={}", username);
        Optional<TraineeResponse> result = traineeRepository.findByUserUsername(username)
                .map(traineeMapper::toResponse);
        if (result.isEmpty()) {
            log.warn("Trainee not found: username={}", username);
        } else {
            log.info("Trainee profile fetched: username={}", username);
        }
        return result;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for trainee: username={}", request.username());
        Trainee trainee = getTraineeByUsername(request.username());
        trainee.getUser().setPassword(passwordEncoder.encode(request.newPassword()));
        traineeRepository.save(trainee);
        log.info("Password changed successfully for trainee: username={}", request.username());
    }

    @Override
    @Transactional
    public TraineeResponse update(String username, UpdateTraineeRequest request) {
        log.info("Updating trainee profile: username={}", username);
        Trainee trainee = getTraineeByUsername(username);

        User user = trainee.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setActive(request.isActive());

        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        TraineeResponse response = traineeMapper.toResponse(traineeRepository.save(trainee));
        log.info("Trainee profile updated: username={}", username);
        return response;
    }

    @Override
    @Transactional
    public void setActive(String username, boolean active) {
        log.info("Setting trainee active={} for username={}", active, username);
        Trainee trainee = getTraineeByUsername(username);
        trainee.getUser().setActive(active);
        traineeRepository.save(trainee);
        log.info("Trainee active status updated: username={}, active={}", username, active);
    }

    @Override
    @Transactional
    public void delete(String username) {
        log.info("Deleting trainee: username={}", username);
        Trainee trainee = getTraineeByUsername(username);
        traineeRepository.delete(trainee);
        log.info("Deleted trainee: username={}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username,
                                               Date fromDate,
                                               Date toDate,
                                               String trainerName,
                                               TrainingType.TrainingTypeName trainingTypeName) {
        log.info("Fetching trainings for trainee: username={}, fromDate={}, toDate={}, trainerName={}",
                username, fromDate, toDate, trainerName);
        List<TrainingResponse> result = trainingRepository
                .findAll(TrainingSpecification.byTraineeCriteria(
                        username, fromDate, toDate, trainerName, trainingTypeName))
                .stream()
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
        log.info("Found {} trainings for trainee: username={}", result.size(), username);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponse> getUnassignedTrainers(String username) {
        log.info("Fetching unassigned trainers for trainee: username={}", username);
        Trainee trainee = getTraineeByUsername(username);
        List<Long> assignedIds = trainee.getTrainers().stream()
                .map(Trainer::getId)
                .toList();
        List<TrainerShortResponse> result = trainerRepository.findAll().stream()
                .filter(trainer -> !assignedIds.contains(trainer.getId()))
                .filter(trainer -> trainer.getUser().isActive())
                .map(trainerMapper::toShortResponse)
                .toList();
        log.info("Found {} unassigned trainers for trainee: username={}", result.size(), username);
        return result;
    }

    @Override
    @Transactional
    public TraineeResponse updateTrainers(String username, UpdateTraineeTrainersRequest request) {
        log.info("Updating trainers for trainee: username={}", username);
        Trainee trainee = getTraineeByUsername(request.username());
        List<Trainer> trainers = trainerRepository
                .findAllByUserUsernameIn(request.trainerUsernames());
        trainee.setTrainers(trainers);
        TraineeResponse response = traineeMapper.toResponse(traineeRepository.save(trainee));
        log.info("Trainers updated for trainee: username={}", username);
        return response;
    }

    private Trainee getTraineeByUsername(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found: username={}", username);
                    return new RuntimeException("Trainee not found: " + username);
                });
    }
}