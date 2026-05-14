package org.example.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.component.TestContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSteps {

    private final TestContext context;

    public TrainingSteps(TestContext context) {
        this.context = context;
    }

    // ─── GIVEN ──────────────────────────────────────────────────────────────────

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
        // Store the training ID if returned; otherwise use a shared lookup
        Response trainings = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredTraineeUsername() + "/trainings");

        Long id = trainings.jsonPath().getLong("[0].id");
        context.setLastTrainingId(id);
    }

    // ─── WHEN ───────────────────────────────────────────────────────────────────

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

    @When("I add a training session with future date")
    public void iAddTrainingWithFutureDate() {
        Map<String, Object> body = buildTrainingBody("Future Session", "CARDIO",
                LocalDate.now().plusDays(10).toString(), 60);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/trainings");

        context.setLastResponse(response);
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

    @When("I add a training session with blank trainer username")
    public void iAddTrainingWithBlankTrainerUsername() {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", context.getRegisteredTraineeUsername());
        body.put("trainerUsername", "");
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

    @When("I add a training without authentication")
    public void iAddTrainingWithoutAuth() {
        Map<String, Object> body = buildTrainingBody("Unauth Training", "CARDIO", "2024-01-01", 60);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/trainings");

        context.setLastResponse(response);
    }

    @When("I get all training types")
    public void iGetAllTrainingTypes() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainings/types");

        context.setLastResponse(response);
    }

    @When("I delete the training session")
    public void iDeleteTheTrainingSession() {
        Long id = context.getLastTrainingId();
        assertThat(id).isNotNull();

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .delete("/api/trainings/" + id);

        context.setLastResponse(response);
    }

    @When("I delete a training with id {long}")
    public void iDeleteTrainingWithId(long id) {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .delete("/api/trainings/" + id);

        context.setLastResponse(response);
    }

    // ─── THEN ───────────────────────────────────────────────────────────────────

    @Then("the response should contain training types")
    public void theResponseShouldContainTrainingTypes() {
        assertThat(context.getLastResponse().jsonPath().getList("$")).isNotEmpty();
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildTrainingBody(String name, String type, String date, int duration) {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", context.getRegisteredTraineeUsername()); // trainee
        body.put("trainerUsername", context.getRegisteredTrainerUsername()); // trainer
        body.put("trainingName", name);
        body.put("typeName", type);
        body.put("trainingDate", date);
        body.put("durationMinutes", duration);
        return body;
    }
}
