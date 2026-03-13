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
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.example.service.AuthService;
import org.example.service.TrainerService;
import org.example.service.UserService;
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

    // ---------------------------------------------------------- registration

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
        log.info("Registered trainer: username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    // ------------------------------------------------------- authentication

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return trainerRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    // ---------------------------------------------------------- find profile

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponse> findByUsername(UserLoginDtoRequest request) {
        authService.authenticate(request.username(), request.password(),
                trainerRepository::existsByUserUsernameAndUserPassword);
        return trainerRepository.findByUserUsername(request.username())
                .map(trainerMapper::toResponse);
    }

    // -------------------------------------------------------- change password

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        authService.authenticate(request.username(), request.oldPassword(),
                trainerRepository::existsByUserUsernameAndUserPassword);

        Trainer trainer = getTrainerByUsername(request.username());
        trainer.getUser().setPassword(request.newPassword());
        trainerRepository.save(trainer);
        log.info("Password changed for trainer: username={}", request.username());
    }

    // ---------------------------------------------------------- update profile

    @Override
    @Transactional
    public TrainerResponse update(UpdateTrainerRequest request) {
        authService.authenticate(request.username(), request.username(),
                trainerRepository::existsByUserUsernameAndUserPassword);

        Trainer trainer = getTrainerByUsername(request.username());
        trainerMapper.updateEntity(request, trainer);
        return trainerMapper.toResponse(trainerRepository.save(trainer));
    }

    // ---------------------------------------------------------- activate

    @Override
    @Transactional
    public void setActive(String username, String password, boolean active) {
        authService.authenticate(username, password,
                trainerRepository::existsByUserUsernameAndUserPassword);

        Trainer trainer = getTrainerByUsername(username);
        trainer.getUser().setActive(active);
        trainerRepository.save(trainer);
        log.info("Trainer active={} for username={}", active, username);
    }

    // --------------------------------------------------------- trainings list

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username,
                                               String password,
                                               Date fromDate,
                                               Date toDate,
                                               String traineeName) {
        authService.authenticate(username, password,
                trainerRepository::existsByUserUsernameAndUserPassword);

        return trainingRepository.findByTrainerUserUsername(username).stream()
                .filter(t -> fromDate == null || !t.getDate().before(fromDate))
                .filter(t -> toDate == null || !t.getDate().after(toDate))
                .filter(t -> traineeName == null ||
                        t.getTrainee().getUser().getUsername().equalsIgnoreCase(traineeName))
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();
    }

    // ----------------------------------------------------------- helper

    private Trainer getTrainerByUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));
    }
}