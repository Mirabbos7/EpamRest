package org.example.health;

import lombok.RequiredArgsConstructor;
import org.example.repository.TrainerRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;

    @Override
    public Health health() {
        try {
            long count = trainerRepository.count();
            return Health.up()
                    .withDetail("trainerCount", count)
                    .withDetail("status", "Trainer repository is accessible")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Trainer repository is not accessible")
                    .build();
        }
    }
}