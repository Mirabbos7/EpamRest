package org.example.integration.steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.example.integration.IntegrationTestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MicroservicesIntegrationSteps {

    private static final String WORKLOAD_BASE_URL = System.getProperty(
            "workload.service.url", "http://localhost:9090");

    private final IntegrationTestContext context;

    public MicroservicesIntegrationSteps(IntegrationTestContext context) {
        this.context = context;
    }

    @Given("both services are running")
    public void bothServicesAreRunning() {
        try {
            Response response = RestAssured
                    .given().baseUri(WORKLOAD_BASE_URL)
                    .get("/api/workload/health");
            assertThat(response.statusCode()).isNotEqualTo(500);
        } catch (Exception e) {
            throw new AssertionError("Workload service is not running at " + WORKLOAD_BASE_URL, e);
        }
    }

    @Given("a trainer is registered with firstName {string} lastName {string} specialization {string}")
    public void aTrainerIsRegistered(String firstName, String lastName, String specialization) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialization", specialization);

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register");

        assertThat(response.statusCode()).isEqualTo(200);
        context.setRegisteredTrainerUsername(response.jsonPath().getString("username"));
        context.setRegisteredTrainerToken(response.jsonPath().getString("token"));
    }

    @Given("a trainee is registered with firstName {string} lastName {string}")
    public void aTraineeIsRegistered(String firstName, String lastName) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");

        assertThat(response.statusCode()).isEqualTo(200);
        context.setRegisteredTraineeUsername(response.jsonPath().getString("username"));
        context.setRegisteredTraineeToken(response.jsonPath().getString("token"));
    }

    @Given("the trainee is authenticated")
    public void theTraineeIsAuthenticated() {
        context.setAuthToken(context.getRegisteredTraineeToken());
        assertThat(context.getAuthToken()).isNotBlank();
    }

    @Given("workload service has {double} hours for trainer in year {int} month {int}")
    public void workloadServiceHasHoursForTrainer(double hours, int year, int month) {
        Response response = getWorkloadHours(context.getRegisteredTrainerUsername(), year, month);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getDouble("workingHours")).isEqualTo(hours);
    }

    @Given("a training session exists with name {string} type {string} date {string} duration {int}")
    public void aTrainingSessionExists(String name, String type, String date, int duration) {
        Map<String, Object> body = buildTrainingBody(name, type, date, duration);
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/trainings");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @When("I add a training session with name {string} type {string} date {string} duration {int}")
    public void iAddTrainingSession(String name, String type, String date, int duration) {
        Map<String, Object> body = buildTrainingBody(name, type, date, duration);
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/trainings");
        context.setLastResponse(response);
    }

    @When("I delete the training session")
    public void iDeleteTheTrainingSession() {
        Response trainings = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredTraineeUsername() + "/trainings");
        Long id = trainings.jsonPath().getLong("[0].id");
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .delete("/api/trainings/" + id);
        context.setLastResponse(response);
    }

    @When("I get a token from the REST service")
    public void iGetTokenFromRestService() {
        assertThat(context.getAuthToken()).isNotBlank();
    }

    @When("I use that token to directly call the Workload service")
    public void iUseTokenToCallWorkloadService() {
        Response response = RestAssured
                .given()
                .baseUri(WORKLOAD_BASE_URL)
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/workload/" + context.getRegisteredTrainerUsername() + "?year=2024&month=6");
        context.setLastResponse(response);
    }

    @When("the REST service sends an invalid workload request directly to workload service")
    public void restServiceSendsInvalidWorkloadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", "");
        Response response = RestAssured
                .given()
                .baseUri(WORKLOAD_BASE_URL)
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");
        context.setLastResponse(response);
    }

    @When("I query workload service for a trainer with no training history")
    public void iQueryWorkloadForTrainerWithNoHistory() {
        context.setLastResponse(getWorkloadHours("completely.unknown.trainer.xyz", 2024, 6));
    }

    @When("the trainer is re-registered with same username")
    public void theTrainerIsReRegistered() {
        assertThat(context.getRegisteredTrainerUsername()).isNotBlank();
    }

    @When("I set the trainer active status to false")
    public void iSetTrainerActiveStatusFalse() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .param("username", context.getRegisteredTrainerUsername())
                .param("isActive", false)
                .patch("/api/trainers/active");
        context.setLastResponse(response);
        assertThat(response.statusCode()).isIn(200, 204);
    }

    @When("I add a training session with blank trainee username")
    public void iAddTrainingWithBlankTraineeUsername() {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", "");
        body.put("trainerUsername", context.getRegisteredTrainerUsername());
        body.put("trainingName", "Test");
        body.put("typeName", "CARDIO");
        body.put("trainingDate", "2024-01-01");
        body.put("durationMinutes", 60);
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/trainings");
        context.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode())
                .as("Expected HTTP status %d but got %d. Body: %s",
                        expectedStatus, context.getLastResponse().statusCode(),
                        context.getLastResponse().body().asString())
                .isEqualTo(expectedStatus);
    }

    @Then("eventually the workload service records {double} hours for the trainer in year {int} month {int}")
    public void eventuallyWorkloadRecordsHours(double expectedHours, int year, int month) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Response response = getWorkloadHours(context.getRegisteredTrainerUsername(), year, month);
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.jsonPath().getDouble("workingHours")).isEqualTo(expectedHours);
                });
    }

    @Then("eventually the workload service reduces hours for the trainer in year {int} month {int}")
    public void eventuallyWorkloadReducesHours(int year, int month) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Response response = getWorkloadHours(context.getRegisteredTrainerUsername(), year, month);
                    assertThat(response.statusCode()).isIn(200, 404);
                    if (response.statusCode() == 200) {
                        assertThat(response.jsonPath().getDouble("workingHours")).isLessThan(2.0);
                    }
                });
    }

    @Then("the workload response status should not be {int}")
    public void workloadResponseStatusShouldNotBe(int unexpectedStatus) {
        assertThat(context.getLastResponse().statusCode()).isNotEqualTo(unexpectedStatus);
    }

    @Then("the workload service has no record for the invalid request")
    public void workloadServiceHasNoRecordForInvalidRequest() {
        Response response = getWorkloadHours("", 2024, 1);
        assertThat(response.statusCode()).isNotEqualTo(200);
    }

    @Then("the workload response status should be {int}")
    public void theWorkloadResponseStatusShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode())
                .as("Expected HTTP status %d but got %d. Body: %s",
                        expectedStatus, context.getLastResponse().statusCode(),
                        context.getLastResponse().body().asString())
                .isEqualTo(expectedStatus);
    }

    private Response getWorkloadHours(String username, int year, int month) {
        return RestAssured
                .given()
                .baseUri(WORKLOAD_BASE_URL)
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/workload/" + username + "?year=" + year + "&month=" + month);
    }

    private Map<String, Object> buildTrainingBody(String name, String type, String date, int duration) {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", context.getRegisteredTraineeUsername());
        body.put("trainerUsername", context.getRegisteredTrainerUsername());
        body.put("trainingName", name);
        body.put("typeName", type);
        body.put("trainingDate", date);
        body.put("durationMinutes", duration);
        return body;
    }
}
