package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.service.JwtTokenService;
import org.example.dto.request.TrainerWorkloadRequest;
import org.example.entity.Training;
import org.example.enums.ActionType;
import org.example.mapper.TrainingWorkloadMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadNotifier {

    private final WorkloadClient workloadClient;
    private final JwtTokenService jwtTokenService;
    private final TrainingWorkloadMapper workloadMapper;

    @CircuitBreaker(name = "workload-service", fallbackMethod = "notifyWorkloadFallback")
    public void notifyWorkload(Training training, ActionType action) {
        String token = "Bearer " + jwtTokenService.generateServiceToken();
        String transactionId = MDC.get("transactionId");

        TrainerWorkloadRequest trainerWorkloadRequest = workloadMapper.toWorkloadRequest(training, action);

        workloadClient.processWorkload(token, transactionId, trainerWorkloadRequest);
        log.info("Notified workload service: action={}, trainer={}",
                action, training.getTrainer().getUser().getUsername());

    }

    public void notifyWorkloadFallback(Training training, ActionType action, Exception e) {
        log.error("Circuit breaker open — workload service unavailable. trainer={}, action={}, error={}",
                training.getTrainer().getUser().getUsername(), action, e.getMessage());
    }
}
