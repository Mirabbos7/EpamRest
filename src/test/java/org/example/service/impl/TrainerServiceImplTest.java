package org.example.service.impl;

import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerDtoRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainingResponse;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.metrics.TrainingMetrics;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.example.config.service.JwtTokenService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserService userService;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private TrainingMetrics trainingMetrics;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private User user;
    private Trainer trainer;
    private TrainingType trainingType;

    private static final String USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(USERNAME);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encoded_pass");
        user.setActive(true);

        trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);
    }

    @Test
    void create_shouldReturnRegistrationResponse() {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "John", "Doe", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser("John", "Doe")).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any())).thenReturn(trainer);
        when(jwtTokenService.generateToken(user)).thenReturn("jwt.token");

        RegistrationResponse response = trainerService.create(request);

        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.token()).isEqualTo("jwt.token");

        verify(userService).createUser("John", "Doe");
        verify(trainingTypeRepository).findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);
        verify(trainerRepository).save(any());
        verify(trainingMetrics).incrementTrainerRegistration();
        verify(jwtTokenService).generateToken(user);
    }

    @Test
    void create_shouldThrow_whenTrainingTypeNotFound() {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "John", "Doe", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser(any(), any())).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TrainingType not found");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void findByUsername_shouldReturnResponse_whenFound() {
        TrainerResponse expected = new TrainerResponse(
                USERNAME, "John", "Doe", TrainingType.TrainingTypeName.CARDIO, true, List.of());

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerResponse> result = trainerService.findByUsername(USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo(USERNAME);

        verify(trainerRepository).findByUserUsername(USERNAME);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = trainerService.findByUsername(USERNAME);

        assertThat(result).isEmpty();
    }

    @Test
    void update_shouldUpdateAndReturnResponse() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                USERNAME, "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO, false);
        TrainerResponse expected = new TrainerResponse(
                USERNAME, "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO, false, List.of());

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        TrainerResponse result = trainerService.update(USERNAME, request);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.isActive()).isFalse();

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.isActive()).isFalse();

        verify(trainerRepository).save(trainer);
    }

    @Test
    void update_shouldThrow_whenTrainerNotFound() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                USERNAME, "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO, true);

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.update(USERNAME, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void setActive_shouldSetActiveTrue() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));

        trainerService.setActive(USERNAME, true);

        assertThat(user.isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActive_shouldSetActiveFalse() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));

        trainerService.setActive(USERNAME, false);

        assertThat(user.isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActive_shouldThrow_whenTrainerNotFound() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.setActive(USERNAME, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    void changePassword_shouldEncodeAndSave() {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "oldPass", "newPass");

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded_new_pass");

        trainerService.changePassword(request);

        assertThat(user.getPassword()).isEqualTo("encoded_new_pass");
        verify(passwordEncoder).encode("newPass");
        verify(trainerRepository).save(trainer);
    }

    @Test
    void changePassword_shouldThrow_whenTrainerNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "old", "new");

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.changePassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void getTrainings_shouldReturnMappedList() {
        TrainingResponse trainingResponse = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, null, "jane.doe");

        when(trainingRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new org.example.entity.Training()));
        when(trainingMapper.toTrainerTrainingResponse(any()))
                .thenReturn(trainingResponse);

        List<TrainingResponse> result = trainerService.getTrainings(USERNAME, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Morning Run");

        verify(trainingRepository).findAll(any(Specification.class));
    }

    @Test
    void getTrainings_shouldReturnEmptyList_whenNoResults() {
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<TrainingResponse> result = trainerService.getTrainings(USERNAME, null, null, null);

        assertThat(result).isEmpty();
    }
}