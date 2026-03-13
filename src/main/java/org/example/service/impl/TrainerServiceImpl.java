package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.repository.*;
import org.example.service.TrainerService;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserService userService;

    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @Override
    public RegistrationResponse create(TrainerDtoRequest request) {

        User user = userService.createUser(
                request.firstName(),
                request.lastName()
        );

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        TrainingType type = new TrainingType();
        type.setTrainingTypeName(request.specialization());

        trainer.setTrainingType(type);

        trainerRepository.save(trainer);

        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return trainerRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    @Override
    public Optional<TrainerResponse> findByUsername(UserLoginDtoRequest request) {

        return trainerRepository.findByUserUsername(request.username())
                .map(trainerMapper::toResponse);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        Trainer trainer = trainerRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        trainer.getUser().setPassword(request.newPassword());
    }

    @Override
    public TrainerResponse update(UpdateTrainerRequest request) {

        Trainer trainer = trainerRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        trainer.getUser().setActive(request.isActive());

        trainer.getTrainingType()
                .setTrainingTypeName(request.specialization());

        return trainerMapper.toResponse(
                trainerRepository.save(trainer)
        );
    }

    @Override
    public void setActive(String username, String password, boolean active) {

        Trainer trainer = trainerRepository
                .findByUserUsername(username)
                .orElseThrow();

        trainer.getUser().setActive(active);
    }

    @Override
    public List<TrainingResponse> getTrainings(String username,
                                               String password,
                                               Date fromDate,
                                               Date toDate,
                                               String traineeName) {

        return trainingRepository.findAll()
                .stream()
                .filter(t -> t.getTrainer().getUser().getUsername().equals(username))
                .map(trainingMapper::toResponse)
                .toList();
    }
}