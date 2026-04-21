package org.example.mapper;

import org.example.dto.response.TrainerResponse;
import org.example.dto.response.TrainerShortResponse;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TrainerMapperImpl.class)
class TrainerMapperTest {

    @Autowired
    private TrainerMapper trainerMapper;

    private Trainer buildTrainer() {
        User user = new User();
        user.setUsername("jane.smith");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);
        trainer.setTrainees(List.of());
        return trainer;
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Trainer trainer = buildTrainer();

        TrainerResponse result = trainerMapper.toResponse(trainer);

        assertThat(result.username()).isEqualTo("jane.smith");
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specialization()).isEqualTo(TrainingType.TrainingTypeName.CARDIO);
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainees()).isEmpty();
    }

    @Test
    void toResponse_shouldReturnNull_whenTrainerIsNull() {
        assertThat(trainerMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_shouldMapTrainees_whenPresent() {
        Trainer trainer = buildTrainer();

        Trainee trainee = new Trainee();
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        trainee.setUser(traineeUser);
        trainer.setTrainees(List.of(trainee));

        TrainerResponse result = trainerMapper.toResponse(trainer);

        assertThat(result.trainees()).hasSize(1);
    }

    @Test
    void toShortResponse_shouldMapAllFields() {
        Trainer trainer = buildTrainer();

        TrainerShortResponse result = trainerMapper.toShortResponse(trainer);

        assertThat(result.username()).isEqualTo("jane.smith");
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specialization()).isEqualTo(TrainingType.TrainingTypeName.CARDIO);
    }

    @Test
    void toShortResponse_shouldReturnNull_whenTrainerIsNull() {
        assertThat(trainerMapper.toShortResponse(null)).isNull();
    }
}