package org.example.component.steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.component.TestContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerSteps {

    private final TestContext context;

    public TrainerSteps(TestContext context) {
        this.context = context;
    }

    // ─── GIVEN ──────────────────────────────────────────────────────────────────

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
        context.setRegisteredUsername(response.jsonPath().getString("username"));
        context.setRegisteredPassword(response.jsonPath().getString("password"));
        context.setAuthToken(response.jsonPath().getString("token"));
        context.setRegisteredTrainerUsername(response.jsonPath().getString("username"));
        context.setRegisteredTrainerToken(response.jsonPath().getString("token"));
        context.setAuthToken(response.jsonPath().getString("token"));
    }

    @Given("the trainer is authenticated")
    public void theTrainerIsAuthenticated() {
        assertThat(context.getAuthToken()).isNotBlank();
    }

    // ─── WHEN ───────────────────────────────────────────────────────────────────

    @When("I register a trainer with firstName {string} lastName {string} specialization {string}")
    public void iRegisterTrainer(String firstName, String lastName, String specialization) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialization", specialization);

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register");

        context.setLastResponse(response);
    }

    @When("I get the trainer profile for the registered trainer")
    public void iGetTrainerProfileForRegistered() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainers/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I get the trainer profile for username {string}")
    public void iGetTrainerProfileByUsername(String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Temp");
        body.put("lastName", "User");
        body.put("specialization", "CARDIO");
        Response reg = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register");
        String tempToken = reg.jsonPath().getString("token");

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + tempToken)
                .get("/api/trainers/" + username);

        context.setLastResponse(response);
    }

    @When("I get the trainer profile without authentication")
    public void iGetTrainerProfileWithoutAuth() {
        Response response = RestAssured
                .given()
                .get("/api/trainers/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I update the trainer profile with firstName {string} lastName {string} specialization {string}")
    public void iUpdateTrainerProfile(String firstName, String lastName, String specialization) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", context.getRegisteredUsername());
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialization", specialization);
        body.put("isActive", true);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .put("/api/trainers");

        context.setLastResponse(response);
    }

    @When("I update the trainer profile without authentication")
    public void iUpdateTrainerProfileWithoutAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Unauthorized");
        body.put("lastName", "User");
        body.put("specialization", "CARDIO");
        body.put("isActive", true);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .put("/api/trainers");

        context.setLastResponse(response);
    }

    @When("I set the trainer active status to {word} for the registered trainer")
    public void iSetTrainerActiveStatus(String status) {
        boolean isActive = Boolean.parseBoolean(status);
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .param("username", context.getRegisteredUsername())
                .param("isActive", isActive)
                .patch("/api/trainers/active");

        context.setLastResponse(response);
    }

    @When("I get trainings for the trainer")
    public void iGetTrainingsForTrainer() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainers/" + context.getRegisteredUsername() + "/trainings");

        context.setLastResponse(response);
    }

    @When("I get trainings for the trainer with fromDate {string} and toDate {string}")
    public void iGetTrainingsForTrainerWithDates(String fromDate, String toDate) {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .param("fromDate", fromDate)
                .param("toDate", toDate)
                .get("/api/trainers/" + context.getRegisteredUsername() + "/trainings");

        context.setLastResponse(response);
    }

    @When("I change the trainer password to {string}")
    public void iChangeTrainerPassword(String newPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", context.getRegisteredUsername());
        body.put("oldPassword", context.getRegisteredPassword());
        body.put("newPassword", newPassword);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .put("/api/trainers/change-password");

        context.setLastResponse(response);
    }

    @When("I register a trainer with blank firstName")
    public void iRegisterTrainerWithBlankFirstName() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "");
        body.put("lastName", "Valid");
        body.put("specialization", "CARDIO");

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register"));
    }

    @When("I register a trainer with blank lastName")
    public void iRegisterTrainerWithBlankLastName() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Valid");
        body.put("lastName", "");
        body.put("specialization", "CARDIO");

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register"));
    }

    @When("I register a trainer without specialization")
    public void iRegisterTrainerWithoutSpecialization() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Valid");
        body.put("lastName", "Also");

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainers/register"));
    }

    // ─── THEN ───────────────────────────────────────────────────────────────────

    @Then("the trainer profile firstName should be {string}")
    public void theTrainerProfileFirstNameShouldBe(String expected) {
        assertThat(context.getLastResponse().jsonPath().getString("firstName")).isEqualTo(expected);
    }

    @Then("the trainer profile lastName should be {string}")
    public void theTrainerProfileLastNameShouldBe(String expected) {
        assertThat(context.getLastResponse().jsonPath().getString("lastName")).isEqualTo(expected);
    }

    @Then("the trainer specialization should be {string}")
    public void theTrainerSpecializationShouldBe(String expected) {
        String actual = context.getLastResponse().jsonPath().getString("specialization");
        assertThat(actual).isEqualToIgnoringCase(expected);
    }
}
