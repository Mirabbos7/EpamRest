package org.example.client;

import org.example.dto.request.TrainerWorkloadRequest;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.User;
import org.example.enums.ActionType;
import org.example.mapper.TrainingWorkloadMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadNotifierTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private TrainingWorkloadMapper workloadMapper;

    @InjectMocks
    private WorkloadNotifier workloadNotifier;

    @Test
    void notifyWorkload_shouldMapAndSendToQueue() {
        ReflectionTestUtils.setField(workloadNotifier, "workloadQueue", "workload.queue");

        User user = new User();
        user.setUsername("john.doe");

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        Training training = new Training();
        training.setTrainer(trainer);

        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        when(workloadMapper.toWorkloadRequest(training, ActionType.ADD)).thenReturn(request);

        workloadNotifier.notifyWorkload(training, ActionType.ADD);

        verify(workloadMapper).toWorkloadRequest(training, ActionType.ADD);
        verify(jmsTemplate).convertAndSend("workload.queue", request);
    }
}