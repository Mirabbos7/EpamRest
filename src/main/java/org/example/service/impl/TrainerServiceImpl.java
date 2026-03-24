package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.metrics.TrainingMetrics;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.example.service.AuthService;
import org.example.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService;
    private final AuthService authService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingMetrics trainingMetrics;

    @Override
    @Transactional
    public RegistrationResponse create(TrainerDtoRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());

        TrainingType trainingType = trainingTypeRepository
                .findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> new RuntimeException(
                        "TrainingType not found: " + request.specialization()));

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);

        trainerRepository.save(trainer);
        trainingMetrics.incrementTrainerRegistration();
        log.info("Registered trainer: username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return trainerRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponse> findByUsername(String username, String password) {
        authenticate(username, password);
        return trainerRepository.findByUserUsername(username)
                .map(trainerMapper::toResponse);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        authenticate(request.username(), request.oldPassword());

        Trainer trainer = getTrainerByUsername(request.username());
        trainer.getUser().setPassword(request.newPassword());
        trainerRepository.save(trainer);
        log.info("Password changed for trainer: username={}", request.username());
    }

    @Override
    @Transactional
    public TrainerResponse update(String username, String password, UpdateTrainerRequest request) {
        authenticate(username, password);

        Trainer trainer = getTrainerByUsername(request.username());
        User user = trainer.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setActive(request.isActive());

        return trainerMapper.toResponse(trainerRepository.save(trainer));
    }

    @Override
    @Transactional
    public void setActive(String username, String password, boolean active) {
        authenticate(username, password);

        Trainer trainer = getTrainerByUsername(username);
        trainer.getUser().setActive(active);
        trainerRepository.save(trainer);
        log.info("Trainer active={} for username={}", active, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username,
                                               String password,
                                               Date fromDate,
                                               Date toDate,
                                               String traineeName) {
        authenticate(username, password);

        return trainingRepository
                .findAll(TrainingSpecification.byTrainerCriteria(
                        username, fromDate, toDate, traineeName))
                .stream()
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();
    }

    private void authenticate(String username, String password) {
        try {
            authService.authenticate(username, password,
                    trainerRepository::existsByUserUsernameAndUserPassword);
        } catch (SecurityException e) {
            trainingMetrics.incrementAuthFailure();
            throw e;
        }
    }

    private Trainer getTrainerByUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));
    }
}