package org.example.service.impl;

import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.*;
import org.example.mapper.TrainerMapperImpl;
import org.example.mapper.TrainingMapperImpl;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private UserService userService;
    @Mock private AuthService authService;

    @Spy private TrainerMapperImpl trainerMapper;
    @Spy private TrainingMapperImpl trainingMapper;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("jane.smith");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setPassword("pass123");
        user.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);
        trainer.setTrainees(List.of());
    }

    @Test
    void findByUsername_shouldReturnMappedResponse() {
        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.of(trainer));

        Optional<TrainerResponse> result =
                trainerService.findByUsername("jane.smith", "pass123");

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("jane.smith");
        assertThat(result.get().firstName()).isEqualTo("Jane");
        assertThat(result.get().specialization())
                .isEqualTo(TrainingType.TrainingTypeName.CARDIO);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.empty());

        Optional<TrainerResponse> result =
                trainerService.findByUsername("jane.smith", "pass123");

        assertThat(result).isEmpty();
    }

    @Test
    void update_shouldReturnMappedResponse() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "jane.smith",
                "Jane",
                "Smith",
                TrainingType.TrainingTypeName.CARDIO,
                true);

        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenReturn(trainer);

        TrainerResponse result =
                trainerService.update("jane.smith", "pass123", request);

        assertThat(result.username()).isEqualTo("jane.smith");
        assertThat(result.specialization())
                .isEqualTo(TrainingType.TrainingTypeName.CARDIO);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void getTrainings_shouldReturnMappedResponses() {
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        Training training = new Training();
        training.setName("Evening Run");
        training.setDate(new Date());
        training.setDurationInMinutes(45);
        training.setTrainingType(trainingType);
        training.setTrainee(trainee);

        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = trainerService.getTrainings(
                "jane.smith", "pass123", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Evening Run");
        assertThat(result.get(0).durationMinutes()).isEqualTo(45);
        verify(trainingMapper).toTrainerTrainingResponse(training);
    }

    @Test
    void create_shouldSaveAndReturnRegistrationResponse() {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser("Jane", "Smith")).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.of(trainer.getTrainingType()));
        when(trainerRepository.save(any())).thenReturn(trainer);

        RegistrationResponse result = trainerService.create(request);

        assertThat(result.username()).isEqualTo("jane.smith");
        assertThat(result.password()).isEqualTo("pass123");
        verify(trainerRepository).save(any());
    }

    @Test
    void create_shouldThrow_whenTrainingTypeNotFound() {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser("Jane", "Smith")).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TrainingType not found");
    }

    @Test
    void matchUsernameAndPassword_shouldReturnTrue_whenExists() {
        when(trainerRepository.existsByUserUsernameAndUserPassword("jane.smith", "pass123"))
                .thenReturn(true);

        boolean result = trainerService.matchUsernameAndPassword("jane.smith", "pass123");

        assertThat(result).isTrue();
    }

    @Test
    void matchUsernameAndPassword_shouldReturnFalse_whenNotExists() {
        when(trainerRepository.existsByUserUsernameAndUserPassword("jane.smith", "wrong"))
                .thenReturn(false);

        boolean result = trainerService.matchUsernameAndPassword("jane.smith", "wrong");

        assertThat(result).isFalse();
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "jane.smith", "pass123", "newpass");

        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenReturn(trainer);

        trainerService.changePassword(request);

        assertThat(trainer.getUser().getPassword()).isEqualTo("newpass");
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActive_shouldUpdateActiveStatus() {
        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenReturn(trainer);

        trainerService.setActive("jane.smith", "pass123", false);

        assertThat(trainer.getUser().isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }
}