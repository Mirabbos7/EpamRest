package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.repository.*;
import org.example.service.TraineeService;
import org.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    @Transactional
    public RegistrationResponse create(TraineeDtoRequest request) {

        User user = userService.createUser(
                request.firstName(),
                request.lastName()
        );

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        traineeRepository.save(trainee);

        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    public boolean matchUsernameAndPassword(String username, String password) {
        return traineeRepository.existsByUserUsernameAndUserPassword(username, password);
    }

    @Override
    public Optional<TraineeResponse> findByUsername(UserLoginDtoRequest request) {

        return traineeRepository.findByUserUsername(request.username())
                .map(traineeMapper::toResponse);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Trainee trainee = traineeRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        if (!trainee.getUser().getPassword().equals(request.oldPassword()))
            throw new RuntimeException("Wrong password");

        trainee.getUser().setPassword(request.newPassword());
    }

    @Override
    @Transactional
    public TraineeResponse update(UpdateTraineeRequest request) {

        Trainee trainee = traineeRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.getUser().setActive(request.isActive());

        return traineeMapper.toResponse(
                traineeRepository.save(trainee)
        );
    }

    @Override
    @Transactional
    public void setActive(String username, String password, boolean active) {

        Trainee trainee = traineeRepository
                .findByUserUsername(username)
                .orElseThrow();

        trainee.getUser().setActive(active);
    }

    @Override
    @Transactional
    public void delete(UserLoginDtoRequest request) {

        Trainee trainee = traineeRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        traineeRepository.delete(trainee);
    }

    @Override
    public List<TrainingResponse> getTrainings(String username,
                                               String password,
                                               Date fromDate,
                                               Date toDate,
                                               String trainerName,
                                               TrainingType.TrainingTypeName type) {

        return trainingRepository.findAll()
                .stream()
                .filter(t -> t.getTrainee().getUser().getUsername().equals(username))
                .map(trainingMapper::toResponse)
                .toList();
    }

    @Override
    public List<TrainerShortResponse> getUnassignedTrainers(UserLoginDtoRequest request) {

        Trainee trainee = traineeRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        return trainerRepository.findAll()
                .stream()
                .filter(tr -> !tr.getTrainees().contains(trainee))
                .map(trainerMapper::toShortResponse)
                .toList();
    }

    @Override
    @Transactional
    public TraineeResponse updateTrainers(UpdateTraineeTrainersRequest request) {

        Trainee trainee = traineeRepository
                .findByUserUsername(request.username())
                .orElseThrow();

        List<Trainer> trainers = trainerRepository
                .findAllByUserUsernameIn(request.trainerUsernames());

        trainee.setTrainers(trainers);

        return traineeMapper.toResponse(
                traineeRepository.save(trainee)
        );
    }
}