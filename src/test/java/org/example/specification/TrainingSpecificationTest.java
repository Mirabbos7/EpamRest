package org.example.specification;

import org.example.entity.*;
import org.example.enums.Role;
import org.example.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainingSpecificationTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private jakarta.persistence.EntityManager em;

    private Training training;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        traineeUser.setActive(true);
        traineeUser.setPassword("pass123");
        traineeUser.setRole(Role.ROLE_USER);
        em.persist(traineeUser);

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        em.persist(trainee);

        User trainerUser = new User();
        trainerUser.setUsername("jane.smith");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Smith");
        trainerUser.setActive(true);
        trainerUser.setPassword("pass123");
        trainerUser.setRole(Role.ROLE_USER);
        em.persist(trainerUser);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingType.TrainingTypeName.CARDIO);
        em.persist(trainingType);

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setTrainingType(trainingType);
        em.persist(trainer);

        training = new Training();
        training.setName("Morning Run");
        training.setDate(new Date());
        training.setDurationInMinutes(60);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        em.persist(training);

        em.flush();
    }

    @Test
    void byTraineeCriteria_shouldReturnTraining_whenAllNulls() {
        Specification<Training> spec = TrainingSpecification
                .byTraineeCriteria("john.doe", null, null, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Morning Run");
    }

    @Test
    void byTraineeCriteria_shouldReturnEmpty_whenTraineeNotMatch() {
        Specification<Training> spec = TrainingSpecification
                .byTraineeCriteria("unknown", null, null, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void byTraineeCriteria_shouldFilterByFromDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();

        Specification<Training> spec = TrainingSpecification
                .byTraineeCriteria("john.doe", tomorrow, null, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void byTraineeCriteria_shouldFilterByToDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();

        Specification<Training> spec = TrainingSpecification
                .byTraineeCriteria("john.doe", null, yesterday, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void byTrainerCriteria_shouldReturnTraining_whenMatchesTrainer() {
        Specification<Training> spec = TrainingSpecification
                .byTrainerCriteria("jane.smith", null, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).hasSize(1);
    }

    @Test
    void byTrainerCriteria_shouldReturnEmpty_whenTrainerNotMatch() {
        Specification<Training> spec = TrainingSpecification
                .byTrainerCriteria("unknown", null, null, null);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void byTraineeCriteria_shouldFilterByTrainingType() {
        Specification<Training> spec = TrainingSpecification
                .byTraineeCriteria("john.doe", null, null, null,
                        TrainingType.TrainingTypeName.CARDIO);

        List<Training> result = trainingRepository.findAll(spec);

        assertThat(result).hasSize(1);
    }
}