package org.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TrainingMetrics {

    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingCreatedCounter;
    private final Counter authFailureCounter;

    public TrainingMetrics(MeterRegistry registry) {
        this.traineeRegistrationCounter = Counter.builder("app.trainee.registrations")
                .description("Total number of trainee registrations")
                .register(registry);

        this.trainerRegistrationCounter = Counter.builder("app.trainer.registrations")
                .description("Total number of trainer registrations")
                .register(registry);

        this.trainingCreatedCounter = Counter.builder("app.training.created")
                .description("Total number of trainings created")
                .register(registry);

        this.authFailureCounter = Counter.builder("app.auth.failures")
                .description("Total number of authentication failures")
                .register(registry);
    }

    public void incrementTraineeRegistration() {
        traineeRegistrationCounter.increment();
    }

    public void incrementTrainerRegistration() {
        trainerRegistrationCounter.increment();
    }

    public void incrementAuthFailure() {
        authFailureCounter.increment();
    }
}