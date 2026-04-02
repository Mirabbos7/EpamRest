package org.example.service.impl;

import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TraineeDtoRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.request.UpdateTraineeTrainersRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.*;
import org.example.mapper.TraineeMapperImpl;
import org.example.mapper.TrainerMapperImpl;
import org.example.mapper.TrainingMapperImpl;
import org.example.metrics.TrainingMetrics;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.config.service.JwtTokenService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private UserService userService;
    @Mock
    private TrainingMetrics trainingMetrics;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private TraineeMapperImpl traineeMapper;
    @Spy
    private TrainerMapperImpl trainerMapper;
    @Spy
    private TrainingMapperImpl trainingMapper;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john.doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("pass123");
        user.setActive(true);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
        trainee.setTrainers(List.of());
    }

    @Test
    void create_shouldSaveAndReturnRegistrationResponse() {
        TraineeDtoRequest request = new TraineeDtoRequest("John", "Doe", null, null);

        when(userService.createUser("John", "Doe")).thenReturn(user);
        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeRepository.save(any())).thenReturn(trainee);
        when(jwtTokenService.generateToken(user)).thenReturn("jwt.token");

        RegistrationResponse result = traineeService.create(request);

        assertThat(result.username()).isEqualTo("john.doe");
        assertThat(result.token()).isEqualTo("jwt.token");
        verify(userService).createUser("John", "Doe");
        verify(traineeMapper).toEntity(request);
        verify(traineeRepository).save(trainee);
        verify(trainingMetrics).incrementTraineeRegistration();
    }

    @Test
    void findByUsername_shouldReturnMappedResponse() {
        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));

        Optional<TraineeResponse> result = traineeService.findByUsername("john.doe");

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("john.doe");
        assertThat(result.get().firstName()).isEqualTo("John");
        assertThat(result.get().lastName()).isEqualTo("Doe");
        assertThat(result.get().isActive()).isTrue();
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.empty());

        Optional<TraineeResponse> result = traineeService.findByUsername("john.doe");

        assertThat(result).isEmpty();
    }

    @Test
    void update_shouldReturnMappedResponse() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Doe", "john.doe", null, "Tashkent", true);

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenReturn(trainee);

        TraineeResponse result = traineeService.update("john.doe", request);

        assertThat(result.username()).isEqualTo("john.doe");
        assertThat(result.address()).isEqualTo("Tashkent");
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void update_shouldThrow_whenTraineeNotFound() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Doe", "unknown", null, null, true);

        when(traineeRepository.findByUserUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.update("unknown", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found");
    }

    @Test
    void changePassword_shouldEncodeAndUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "john.doe", "pass123", "newpass");

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_newpass");
        when(traineeRepository.save(any())).thenReturn(trainee);

        traineeService.changePassword(request);

        assertThat(trainee.getUser().getPassword()).isEqualTo("encoded_newpass");
        verify(passwordEncoder).encode("newpass");
        verify(traineeRepository).save(trainee);
    }

    @Test
    void setActive_shouldUpdateActiveStatus() {
        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenReturn(trainee);

        traineeService.setActive("john.doe", false);

        assertThat(trainee.getUser().isActive()).isFalse();
        verify(traineeRepository).save(trainee);
    }

    @Test
    void delete_shouldDeleteTrainee() {
        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));

        traineeService.delete("john.doe");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void delete_shouldThrow_whenTraineeNotFound() {
        when(traineeRepository.findByUserUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found");
    }

    @Test
    void getTrainings_shouldReturnMappedResponses() {
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        Training training = new Training();
        training.setName("Morning Run");
        training.setDate(new Date());
        training.setDurationInMinutes(60);
        training.setTrainingType(trainingType);
        training.setTrainer(trainer);

        when(trainingRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = traineeService.getTrainings(
                "john.doe", null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Morning Run");
        assertThat(result.get(0).durationMinutes()).isEqualTo(60);
        verify(trainingMapper).toTraineeTrainingResponse(training);
    }

    @Test
    void getUnassignedTrainers_shouldReturnMappedShortResponses() {
        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Smith");
        trainerUser.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        Trainer trainer = new Trainer();
        trainer.setId(10L);
        trainer.setUser(trainerUser);
        trainer.setTrainingType(trainingType);

        trainee.setTrainers(List.of());

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<TrainerShortResponse> result = traineeService.getUnassignedTrainers("john.doe");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("jane.smith");
        assertThat(result.get(0).specialization()).isEqualTo(TrainingType.TrainingTypeName.CARDIO);
        verify(trainerMapper).toShortResponse(trainer);
    }

    @Test
    void getUnassignedTrainers_shouldExcludeAlreadyAssigned() {
        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");
        trainerUser.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setId(10L);
        trainer.setUser(trainerUser);

        trainee.setTrainers(List.of(trainer));

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<TrainerShortResponse> result = traineeService.getUnassignedTrainers("john.doe");

        assertThat(result).isEmpty();
    }

    @Test
    void getUnassignedTrainers_shouldExcludeInactiveTrainers() {
        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");
        trainerUser.setActive(false);

        Trainer trainer = new Trainer();
        trainer.setId(10L);
        trainer.setUser(trainerUser);

        trainee.setTrainers(List.of());

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<TrainerShortResponse> result = traineeService.getUnassignedTrainers("john.doe");

        assertThat(result).isEmpty();
    }

    @Test
    void updateTrainers_shouldReturnMappedResponse() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "john.doe", List.of("jane.smith"));

        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Smith");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUserUsernameIn(List.of("jane.smith")))
                .thenReturn(List.of(trainer));
        when(traineeRepository.save(any())).thenReturn(trainee);

        TraineeResponse result = traineeService.updateTrainers("john.doe", request);

        assertThat(result.username()).isEqualTo("john.doe");
        verify(traineeMapper).toResponse(trainee);
    }
}