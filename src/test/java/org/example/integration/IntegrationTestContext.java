package org.example.integration;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class IntegrationTestContext {

    private Response lastResponse;
    private String registeredUsername;
    private String registeredPassword;
    private String authToken;
    private Long lastTrainingId;
    private String registeredTraineeUsername;
    private String registeredTraineeToken;
    private String registeredTrainerUsername;
    private String registeredTrainerToken;

    public void reset() {
        lastResponse = null;
        registeredUsername = null;
        registeredPassword = null;
        authToken = null;
        lastTrainingId = null;
        registeredTraineeUsername = null;
        registeredTraineeToken = null;
        registeredTrainerUsername = null;
        registeredTrainerToken = null;
    }
}
