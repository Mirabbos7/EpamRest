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
import org.example.security.service.JwtTokenService;
import org.example.service.AuthService;
import org.example.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingMetrics trainingMetrics;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;


    @Override
    @Transactional
    public RegistrationResponse create(TrainerDtoRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());

        TrainingType trainingType = trainingTypeRepository
                .findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> {
                    log.error("TrainingType not found: {}", request.specialization());
                    return new RuntimeException("TrainingType not found: " + request.specialization());
                });

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);
        trainerRepository.save(trainer);

        trainingMetrics.incrementTrainerRegistration();

        String token = jwtTokenService.generateToken(user);
        log.info("Registered trainer: username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword(), token);
    }

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return trainerRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponse> findByUsername(String username) {
        log.info("Fetching trainer profile: username={}", username);
        Optional<TrainerResponse> result = trainerRepository.findByUserUsername(username)
                .map(trainerMapper::toResponse);
        if (result.isEmpty()) {
            log.warn("Trainer not found: username={}", username);
        } else {
            log.info("Trainer profile fetched: username={}", username);
        }
        return result;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for trainer: username={}", request.username());
        Trainer trainer = getTrainerByUsername(request.username());
        trainer.getUser().setPassword(passwordEncoder.encode(request.newPassword()));
        trainerRepository.save(trainer);
        log.info("Password changed successfully for trainer: username={}", request.username());
    }

    @Override
    @Transactional
    public TrainerResponse update(String username, UpdateTrainerRequest request) {
        log.info("Updating trainer profile: username={}", username);
        Trainer trainer = getTrainerByUsername(username);
        User user = trainer.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setActive(request.isActive());
        TrainerResponse response = trainerMapper.toResponse(trainerRepository.save(trainer));
        log.info("Trainer profile updated: username={}", username);
        return response;
    }

    @Override
    @Transactional
    public void setActive(String username, boolean active) {
        log.info("Setting trainer active={} for username={}", active, username);
        Trainer trainer = getTrainerByUsername(username);
        trainer.getUser().setActive(active);
        trainerRepository.save(trainer);
        log.info("Trainer active status updated: username={}, active={}", username, active);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username, Date fromDate,
                                               Date toDate, String traineeName) {
        log.info("Fetching trainings for trainer: username={}, fromDate={}, toDate={}, traineeName={}",
                username, fromDate, toDate, traineeName);
        List<TrainingResponse> result = trainingRepository
                .findAll(TrainingSpecification.byTrainerCriteria(username, fromDate, toDate, traineeName))
                .stream()
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();
        log.info("Found {} trainings for trainer: username={}", result.size(), username);
        return result;
    }

    private Trainer getTrainerByUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainer not found: username={}", username);
                    return new RuntimeException("Trainer not found: " + username);
                });
    }
}