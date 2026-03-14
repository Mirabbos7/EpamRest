package org.example.service.impl;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.*;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private TrainerMapper trainerMapper;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private User user;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("jane.smith");
        user.setPassword("pass123");
        user.setFirstName("Jane");
        user.setLastName("Smith");
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
                "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser("Jane", "Smith")).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        RegistrationResponse response = trainerService.create(request);

        assertEquals("jane.smith", response.username());
        assertEquals("pass123", response.password());
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void create_shouldThrow_whenTrainingTypeNotFound() {
        TrainerDtoRequest request = new TrainerDtoRequest(
                "Jane", "Smith", TrainingType.TrainingTypeName.CARDIO);

        when(userService.createUser("Jane", "Smith")).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.create(request));
    }


    @Test
    void matchUsernameAndPassword_shouldReturnTrue_whenValid() {
        when(trainerRepository.existsByUserUsernameAndUserPassword("jane.smith", "pass123"))
                .thenReturn(true);

        assertTrue(trainerService.matchUsernameAndPassword("jane.smith", "pass123"));
    }

    @Test
    void matchUsernameAndPassword_shouldReturnFalse_whenInvalid() {
        when(trainerRepository.existsByUserUsernameAndUserPassword("jane.smith", "wrong"))
                .thenReturn(false);

        assertFalse(trainerService.matchUsernameAndPassword("jane.smith", "wrong"));
    }

    @Test
    void findByUsername_shouldReturnResponse_whenFound() {
        TrainerResponse expected = new TrainerResponse(
                "jane.smith", "Jane", "Smith",
                TrainingType.TrainingTypeName.CARDIO, true, List.of());

        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerResponse> result = trainerService.findByUsername("jane.smith", "pass123");

        assertTrue(result.isPresent());
        assertEquals("jane.smith", result.get().username());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = trainerService.findByUsername("jane.smith", "pass123");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_shouldThrow_whenInvalidCredentials() {
        doThrow(new SecurityException("Invalid credentials: jane.smith"))
                .when(authService).authenticate(eq("jane.smith"), eq("wrong"), any());

        assertThrows(SecurityException.class, () ->
                trainerService.findByUsername("jane.smith", "wrong"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "jane.smith", "pass123", "newPass");

        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.changePassword(request);

        assertEquals("newPass", trainer.getUser().getPassword());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void changePassword_shouldThrow_whenTrainerNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "unknown", "pass", "newPass");

        doNothing().when(authService).authenticate(eq("unknown"), eq("pass"), any());
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.changePassword(request));
    }

    @Test
    void update_shouldUpdateFieldsAndReturnResponse() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "jane.smith", "NewName", "NewLastName",
                TrainingType.TrainingTypeName.STRENGTH, false);

        TrainerResponse expected = new TrainerResponse(
                "jane.smith", "NewName", "NewLastName",
                TrainingType.TrainingTypeName.STRENGTH, false, List.of());

        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        TrainerResponse result = trainerService.update("jane.smith", "pass123", request);

        assertEquals("NewName", trainer.getUser().getFirstName());
        assertEquals("NewLastName", trainer.getUser().getLastName());
        assertFalse(trainer.getUser().isActive());
        assertEquals("NewName", result.firstName());
    }

    @Test
    void update_shouldThrow_whenTrainerNotFound() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "unknown", "Name", "Last",
                TrainingType.TrainingTypeName.CARDIO, true);

        doNothing().when(authService).authenticate(eq("unknown"), eq("pass"), any());
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                trainerService.update("unknown", "pass", request));
    }


    @Test
    void setActive_shouldSetActiveTrue() {
        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.setActive("jane.smith", "pass123", true);

        assertTrue(trainer.getUser().isActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActive_shouldSetActiveFalse() {
        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.setActive("jane.smith", "pass123", false);

        assertFalse(trainer.getUser().isActive());
    }

    @Test
    void getTrainings_shouldReturnAllTrainings_whenNoFilters() {
        Training training = new Training();
        training.setDate(new Date());
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        training.setTrainee(trainee);

        TrainingResponse trainingResponse = new TrainingResponse(
                "Morning Cardio", new Date(), "CARDIO", 60, null, "jane.smith");

        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainerService.getTrainings(
                "jane.smith", "pass123", null, null, null);

        assertEquals(1, result.size());
        verify(trainingMapper).toTrainerTrainingResponse(training);
    }

    @Test
    void getTrainings_shouldFilterByTraineeName() {
        Training matchingTraining = new Training();
        matchingTraining.setDate(new Date());
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        matchingTraining.setTrainee(trainee);

        Training otherTraining = new Training();
        otherTraining.setDate(new Date());
        User otherUser = new User();
        otherUser.setUsername("other.user");
        Trainee otherTrainee = new Trainee();
        otherTrainee.setUser(otherUser);
        otherTraining.setTrainee(otherTrainee);

        TrainingResponse response = new TrainingResponse(
                "Morning Cardio", new Date(), "CARDIO", 60, null, "john.doe");

        doNothing().when(authService).authenticate(eq("jane.smith"), eq("pass123"), any());
        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(matchingTraining, otherTraining));
        when(trainingMapper.toTrainerTrainingResponse(matchingTraining)).thenReturn(response);

        List<TrainingResponse> result = trainerService.getTrainings(
                "jane.smith", "pass123", null, null, "john.doe");

        assertEquals(1, result.size());
        assertEquals("john.doe", result.get(0).traineeName());
    }

    @Test
    void getTrainings_shouldThrow_whenInvalidCredentials() {
        doThrow(new SecurityException("Invalid credentials: jane.smith"))
                .when(authService).authenticate(eq("jane.smith"), eq("wrong"), any());

        assertThrows(SecurityException.class, () ->
                trainerService.getTrainings("jane.smith", "wrong", null, null, null));
    }
}