package org.example.health;

import lombok.RequiredArgsConstructor;
import org.example.repository.TraineeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraineeHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;

    @Override
    public Health health() {
        try {
            long count = traineeRepository.count();
            return Health.up()
                    .withDetail("traineeCount", count)
                    .withDetail("status", "Trainee repository is accessible")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Trainee repository is not accessible")
                    .build();
        }
    }
}