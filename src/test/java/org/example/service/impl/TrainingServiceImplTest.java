package org.example.service.impl;

import org.example.client.WorkloadNotifier;
import org.example.dto.request.TrainingDtoRequest;
import org.example.dto.response.TrainingResponse;
import org.example.entity.*;
import org.example.mapper.TrainingMapperImpl;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private WorkloadNotifier workloadNotifier;

    @Spy
    private TrainingMapperImpl trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainer trainer;
    private Trainee trainee;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");

        trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setTrainingType(trainingType);

        User traineeUser = new User();
        traineeUser.setUsername("john.doe");

        trainee = new Trainee();
        trainee.setUser(traineeUser);
    }

    @Test
    void create_shouldSaveAndReturnMappedResponse() {
        TrainingDtoRequest request = new TrainingDtoRequest(
                "john.doe", "jane.smith", "Morning Run",
                TrainingType.TrainingTypeName.CARDIO, new Date(), 60);

        when(trainerRepository.findByUserUsername("jane.smith"))
                .thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUserUsername("john.doe"))
                .thenReturn(Optional.of(trainee));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.TrainingTypeName.CARDIO))
                .thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // TODO:
        //  Source code is supposed to be in English and comments are the part of it.
        //  You can find all cyrillic entries by using this regex: [а-яА-ЯёЁ]
        doNothing().when(workloadNotifier).notifyWorkload(any(), any()); // ← сюда

        TrainingResponse result = trainingService.create(request);

        assertThat(result.trainingName()).isEqualTo("Morning Run");
        assertThat(result.durationMinutes()).isEqualTo(60);
        verify(trainingMapper).toTraineeTrainingResponse(any());
    }

    @Test
    void getTraineeTrainings_shouldReturnFilteredAndMapped() {
        Training training = new Training();
        training.setName("Yoga");
        training.setDate(new Date());
        training.setDurationInMinutes(30);
        training.setTrainingType(trainingType);
        training.setTrainer(trainer);

        when(trainingRepository.findByTraineeUserUsername("john.doe"))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTraineeTrainings(
                "john.doe", null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Yoga");
        verify(trainingMapper).toTraineeTrainingResponse(training);
    }

    @Test
    void getTrainerTrainings_shouldReturnFilteredAndMapped() {
        Training training = new Training();
        training.setName("Pilates");
        training.setDate(new Date());
        training.setDurationInMinutes(50);
        training.setTrainingType(trainingType);
        training.setTrainee(trainee);

        when(trainingRepository.findByTrainerUserUsername("jane.smith"))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = trainingService.getTrainerTrainings(
                "jane.smith", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Pilates");
        verify(trainingMapper).toTrainerTrainingResponse(training);
    }

    @Test
    void select_shouldReturnMappedResponse_whenFound() {
        Training training = new Training();
        training.setName("Morning Run");
        training.setDate(new Date());
        training.setDurationInMinutes(60);
        training.setTrainingType(trainingType);
        training.setTrainer(trainer);

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        Optional<TrainingResponse> result = trainingService.select(1L);

        assertThat(result).isPresent();
        assertThat(result.get().trainingName()).isEqualTo("Morning Run");
        verify(trainingMapper).toTraineeTrainingResponse(training);
    }

    @Test
    void select_shouldReturnEmpty_whenNotFound() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = trainingService.select(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainingsForTraineesNextWeek_shouldReturnOnlyMatchingTrainings() {
        Calendar cal = Calendar.getInstance();

        // завтра — попадает в диапазон
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();

        // 10 дней — не попадает в диапазон (больше 7 дней)
        cal.add(Calendar.DAY_OF_YEAR, 9);
        Date tenDaysLater = cal.getTime();

        Trainee matchingTrainee = new Trainee();
        matchingTrainee.setId(1L);
        matchingTrainee.setUser(trainee.getUser());

        Training withinRange = new Training();
        withinRange.setName("Yoga");
        withinRange.setDate(tomorrow);
        withinRange.setDurationInMinutes(30);
        withinRange.setTrainingType(trainingType);
        withinRange.setTrainer(trainer);
        withinRange.setTrainee(matchingTrainee);

        Training outOfRange = new Training();
        outOfRange.setName("Pilates");
        outOfRange.setDate(tenDaysLater);
        outOfRange.setDurationInMinutes(45);
        outOfRange.setTrainingType(trainingType);
        outOfRange.setTrainer(trainer);
        outOfRange.setTrainee(matchingTrainee);

        when(trainingRepository.findAll()).thenReturn(List.of(withinRange, outOfRange));

        List<TrainingResponse> result =
                trainingService.getTrainingsForTraineesNextWeek(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Yoga");
        verify(trainingMapper).toTraineeTrainingResponse(withinRange);
        verify(trainingMapper, never()).toTraineeTrainingResponse(outOfRange);
    }

    @Test
    void getTrainingsForTraineesNextWeek_shouldReturnEmpty_whenNoMatchingTrainees() {
        Trainee otherTrainee = new Trainee();
        otherTrainee.setId(99L);
        otherTrainee.setUser(trainee.getUser());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);

        Training training = new Training();
        training.setName("Yoga");
        training.setDate(cal.getTime());
        training.setDurationInMinutes(30);
        training.setTrainingType(trainingType);
        training.setTrainer(trainer);
        training.setTrainee(otherTrainee);

        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<TrainingResponse> result =
                trainingService.getTrainingsForTraineesNextWeek(List.of(1L));

        assertThat(result).isEmpty();
    }
}