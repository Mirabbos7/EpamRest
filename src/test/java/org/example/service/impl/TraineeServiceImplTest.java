package org.example.service.impl;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.*;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private TraineeMapper traineeMapper;
    @Mock private TrainerMapper trainerMapper;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private User user;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john.doe");
        user.setPassword("pass123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);

        trainee = new Trainee();
        trainee.setUser(user);
    }

    @Test
    void create_shouldReturnRegistrationResponse() {
        TraineeDtoRequest request = new TraineeDtoRequest("John", "Doe", null, null);

        when(userService.createUser("John", "Doe")).thenReturn(user);
        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        RegistrationResponse response = traineeService.create(request);

        assertEquals("john.doe", response.username());
        assertEquals("pass123", response.password());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void matchUsernameAndPassword_shouldReturnTrue_whenValid() {
        when(traineeRepository.existsByUserUsernameAndUserPassword("john.doe", "pass123"))
                .thenReturn(true);

        assertTrue(traineeService.matchUsernameAndPassword("john.doe", "pass123"));
    }

    @Test
    void matchUsernameAndPassword_shouldReturnFalse_whenInvalid() {
        when(traineeRepository.existsByUserUsernameAndUserPassword("john.doe", "wrong"))
                .thenReturn(false);

        assertFalse(traineeService.matchUsernameAndPassword("john.doe", "wrong"));
    }

    @Test
    void findByUsername_shouldReturnResponse_whenFound() {
        TraineeResponse expected = new TraineeResponse(
                "john.doe", "John", "Doe", null, null, true, List.of());

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Optional<TraineeResponse> result = traineeService.findByUsername("john.doe", "pass123");

        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().username());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        Optional<TraineeResponse> result = traineeService.findByUsername("john.doe", "pass123");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_shouldThrow_whenInvalidCredentials() {
        doThrow(new SecurityException("Invalid credentials: john.doe"))
                .when(authService).authenticate(eq("john.doe"), eq("wrong"), any());

        assertThrows(SecurityException.class, () ->
                traineeService.findByUsername("john.doe", "wrong"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("john.doe", "pass123", "newPass");

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.changePassword(request);

        assertEquals("newPass", trainee.getUser().getPassword());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void changePassword_shouldThrow_whenTraineeNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest("unknown", "pass", "newPass");

        doNothing().when(authService).authenticate(eq("unknown"), eq("pass"), any());
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.changePassword(request));
    }

    @Test
    void update_shouldUpdateFieldsAndReturnResponse() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "Jane", "Doe", "john.doe", null, "Tashkent", true);
        TraineeResponse expected = new TraineeResponse(
                "john.doe", "Jane", "Doe", null, "Tashkent", true, List.of());

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        TraineeResponse result = traineeService.update("john.doe", "pass123", request);

        assertEquals("Jane", trainee.getUser().getFirstName());
        assertEquals("Tashkent", trainee.getAddress());
        assertEquals("jane", result.firstName().toLowerCase());
    }

    @Test
    void setActive_shouldSetActiveTrue() {
        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.setActive("john.doe", "pass123", true);

        assertTrue(trainee.getUser().isActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void setActive_shouldSetActiveFalse() {
        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.setActive("john.doe", "pass123", false);

        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void delete_shouldDeleteTrainee() {
        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.delete("john.doe", "pass123");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void delete_shouldThrow_whenTraineeNotFound() {
        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                traineeService.delete("john.doe", "pass123"));
    }

    @Test
    void getTrainings_shouldReturnFilteredList() {
        Training training = new Training();
        training.setDate(new Date());
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        TrainingResponse trainingResponse = new TrainingResponse(
                "Morning run", new Date(), "CARDIO", 60, "john.doe", null);

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = traineeService.getTrainings(
                "john.doe", "pass123", null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getUnassignedTrainers_shouldReturnActiveUnassignedTrainers() {
        Trainer assignedTrainer = new Trainer();
        assignedTrainer.setId(1L);
        User assignedUser = new User();
        assignedUser.setActive(true);
        assignedTrainer.setUser(assignedUser);

        Trainer unassignedTrainer = new Trainer();
        unassignedTrainer.setId(2L);
        User unassignedUser = new User();
        unassignedUser.setActive(true);
        unassignedTrainer.setUser(unassignedUser);

        trainee.setTrainers(List.of(assignedTrainer));

        TrainerShortResponse shortResponse = new TrainerShortResponse(
                "jane.smith", "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(assignedTrainer, unassignedTrainer));
        when(trainerMapper.toShortResponse(unassignedTrainer)).thenReturn(shortResponse);

        List<TrainerShortResponse> result =
                traineeService.getUnassignedTrainers("john.doe", "pass123");

        assertEquals(1, result.size());
        assertEquals("jane.smith", result.get(0).username());
    }

    @Test
    void updateTrainers_shouldUpdateTrainerList() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "john.doe", List.of("jane.smith"));

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        TraineeResponse expected = new TraineeResponse(
                "john.doe", "John", "Doe", null, null, true, List.of());

        doNothing().when(authService).authenticate(eq("john.doe"), eq("pass123"), any());
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUserUsernameIn(List.of("jane.smith")))
                .thenReturn(List.of(trainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        TraineeResponse result =
                traineeService.updateTrainers("john.doe", "pass123", request);

        assertEquals(1, trainee.getTrainers().size());
        assertEquals("john.doe", result.username());
    }
}