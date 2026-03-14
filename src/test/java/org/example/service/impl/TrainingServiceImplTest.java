package org.example.service.impl;

import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.entity.*;
import org.example.mapper.TrainingMapper;
import org.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private User traineeUser;
    private User trainerUser;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;
    private TrainingDtoRequest request;
    private TrainingResponse trainingResponse;

    @BeforeEach
    void setUp() {
        traineeUser = new User();
        traineeUser.setUsername("john.doe");

        trainerUser = new User();
        trainerUser.setUsername("jane.smith");

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(traineeUser);

        trainer = new Trainer();
        trainer.setUser(trainerUser);

        trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        training = new Training();
        training.setName("Morning Run");
        training.setDate(new Date());
        training.setDurationInMinutes(60);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        request = new TrainingDtoRequest(
                "john.doe", "jane.smith", "Morning Run",
                TrainingType.TrainingTypeName.CARDIO, new Date(), 60);

        trainingResponse = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, "jane.smith", null);
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenReturn(training);
        when(trainingMapper.toTraineeTrainingResponse(any(Training.class))).thenReturn(trainingResponse);

        TrainingResponse result = trainingService.create(request);

        assertNotNull(result);
        assertEquals("Morning Run", result.trainingName());
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void create_shouldThrow_whenTrainerNotFound() {
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainingService.create(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenTraineeNotFound() {
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainingService.create(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenTrainingTypeNotFound() {
        when(trainerRepository.findByUserUsername("jane.smith")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainingService.create(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void select_shouldReturnResponse_whenFound() {
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        Optional<TrainingResponse> result = trainingService.select(1L);

        assertTrue(result.isPresent());
        assertEquals("Morning Run", result.get().trainingName());
    }

    @Test
    void select_shouldReturnEmpty_whenNotFound() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = trainingService.select(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTraineeTrainings_shouldReturnAll_whenNoFilters() {
        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTraineeTrainings_shouldFilterByDateRange() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date tomorrow = cal.getTime();

        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", yesterday, tomorrow, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTraineeTrainings_shouldFilterOutByDateRange() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date dayAfterTomorrow = cal.getTime();

        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", tomorrow, dayAfterTomorrow, null, null);

        assertEquals(0, result.size());
    }

    @Test
    void getTraineeTrainings_shouldFilterByTrainerUsername() {
        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", null, null, "jane.smith", null);

        assertEquals(1, result.size());
    }

    @Test
    void getTraineeTrainings_shouldFilterByTrainingType() {
        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", null, null, null, TrainingType.TrainingTypeName.CARDIO);

        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldReturnAll_whenNoFilters() {
        TrainingResponse trainerResponse = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, null, "john.doe");

        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(trainerResponse);

        List<TrainingResponse> result = trainingService.getTrainerTrainings(
                "jane.smith", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldFilterByTraineeName() {
        TrainingResponse trainerResponse = new TrainingResponse(
                "Morning Run", new Date(), "CARDIO", 60, null, "john.doe");

        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));
        when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(trainerResponse);

        List<TrainingResponse> result = trainingService.getTrainerTrainings(
                "jane.smith", null, null, "john.doe");

        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldReturnEmpty_whenTraineeNameNotMatch() {
        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTrainerTrainings(
                "jane.smith", null, null, "other.user");

        assertEquals(0, result.size());
    }

    @Test
    void getTrainingsForTraineesNextWeek_shouldReturnTrainingsWithinNextWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3);
        training.setDate(cal.getTime());

        when(trainingRepository.findAll()).thenReturn(List.of(training));
        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        List<TrainingResponse> result = trainingService.getTrainingsForTraineesNextWeek(
                List.of(1L));

        assertEquals(1, result.size());
    }

    @Test
    void getTrainingsForTraineesNextWeek_shouldExcludeTrainingsOutsideNextWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 10);
        training.setDate(cal.getTime());

        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTrainingsForTraineesNextWeek(
                List.of(1L));

        assertEquals(0, result.size());
    }

    @Test
    void getTrainingsForTraineesNextWeek_shouldExcludeOtherTrainees() {
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTrainingsForTraineesNextWeek(
                List.of(99L));

        assertEquals(0, result.size());
    }
}