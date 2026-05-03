package org.example.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.TrainerWorkloadRequest;
import org.example.entity.Training;
import org.example.enums.ActionType;
import org.example.mapper.TrainingWorkloadMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadNotifier {

    @Value("${activemq.queue.workload}")
    private String workloadQueue;

    private final JmsTemplate jmsTemplate;
    private final TrainingWorkloadMapper workloadMapper;

    public void notifyWorkload(Training training, ActionType action) {
        TrainerWorkloadRequest trainerWorkloadRequest = workloadMapper.toWorkloadRequest(training, action);

        String txId = MDC.get("txId");

        jmsTemplate.convertAndSend(workloadQueue, trainerWorkloadRequest, message -> {
            message.setStringProperty("txId", txId != null ? txId : "");
            return message;
        });

        log.info("Sent workload event to queue: queue={}, action={}, trainer={}",
                workloadQueue, action, training.getTrainer().getUser().getUsername());
    }
}
