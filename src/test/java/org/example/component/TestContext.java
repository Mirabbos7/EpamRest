package org.example.component;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Shared context passed between step definition classes within one Cucumber scenario.
 */
@Component
@Getter
@Setter
public class TestContext {

    private Response lastResponse;
    private String registeredUsername;
    private String registeredPassword;
    private String authToken;
    private Long lastTrainingId;
    private String registeredTrainerUsername;
    private String registeredTrainerToken;
    private String registeredTraineeUsername;
    private String registeredTraineeToken;

    public void reset() {
        lastResponse = null;
        registeredUsername = null;
        registeredPassword = null;
        authToken = null;
        lastTrainingId = null;
        registeredTrainerUsername = null;
        registeredTrainerToken = null;
        registeredTraineeUsername = null;
        registeredTraineeToken = null;
    }
}
