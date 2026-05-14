package org.example.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.component.TestContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TraineeSteps {

    private final TestContext context;

    public TraineeSteps(TestContext context) {
        this.context = context;
    }

    // ─── GIVEN ──────────────────────────────────────────────────────────────────

    @Given("the application is running")
    public void theApplicationIsRunning() {
        // Spring Boot test context is started by CucumberSpringConfiguration
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
        context.setRegisteredUsername(response.jsonPath().getString("username"));
        context.setRegisteredPassword(response.jsonPath().getString("password"));
        context.setAuthToken(response.jsonPath().getString("token"));
    }

    @Given("the trainee is authenticated")
    public void theTraineeIsAuthenticated() {
        // Token is already set during registration
        assertThat(context.getAuthToken()).isNotBlank();
    }

    // ─── WHEN ───────────────────────────────────────────────────────────────────

    @When("I register a trainee with firstName {string} lastName {string} address {string}")
    public void iRegisterTraineeWithAddress(String firstName, String lastName, String address) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("address", address);

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");

        context.setLastResponse(response);
    }

    @When("I register a trainee with firstName {string} lastName {string} without optional fields")
    public void iRegisterTraineeWithoutOptionalFields(String firstName, String lastName) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");

        context.setLastResponse(response);
    }

    @When("I get the trainee profile for the registered trainee")
    public void iGetTraineeProfileForRegistered() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I get the trainee profile for username {string}")
    public void iGetTraineeProfileByUsername(String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Temp");
        body.put("lastName", "User");
        Response reg = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");
        String tempToken = reg.jsonPath().getString("token");

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + tempToken)
                .get("/api/trainees/" + username);

        context.setLastResponse(response);
    }

    @When("I get the trainee profile without authentication")
    public void iGetTraineeProfileWithoutAuth() {
        Response response = RestAssured
                .given()
                .get("/api/trainees/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I update the trainee profile with firstName {string} lastName {string} address {string}")
    public void iUpdateTraineeProfile(String firstName, String lastName, String address) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", context.getRegisteredUsername());
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("address", address);
        body.put("isActive", true);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .put("/api/trainees/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I delete the trainee")
    public void iDeleteTrainee() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .delete("/api/trainees/" + context.getRegisteredUsername());

        context.setLastResponse(response);
    }

    @When("I set the trainee active status to {word}")
    public void iSetTraineeActiveStatus(String status) {
        boolean isActive = Boolean.parseBoolean(status);
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .param("isActive", isActive)
                .patch("/api/trainees/" + context.getRegisteredUsername() + "/active");

        context.setLastResponse(response);
    }

    @When("I get unassigned trainers for the trainee")
    public void iGetUnassignedTrainers() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredUsername() + "/trainers/unassigned");

        context.setLastResponse(response);
    }

    @When("I get trainings for the trainee")
    public void iGetTrainingsForTrainee() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredUsername() + "/trainings");

        context.setLastResponse(response);
    }

    @When("I get trainings for the trainee with fromDate {string} and toDate {string}")
    public void iGetTrainingsWithDateRange(String fromDate, String toDate) {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .param("fromDate", fromDate)
                .param("toDate", toDate)
                .get("/api/trainees/" + context.getRegisteredUsername() + "/trainings");

        context.setLastResponse(response);
    }

    @When("I register a trainee with blank firstName")
    public void iRegisterTraineeWithBlankFirstName() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "");
        body.put("lastName", "Valid");

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");

        context.setLastResponse(response);
    }

    @When("I register a trainee with blank lastName")
    public void iRegisterTraineeWithBlankLastName() {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Valid");
        body.put("lastName", "");

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/trainees/register");

        context.setLastResponse(response);
    }

    // ─── THEN ───────────────────────────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode())
                .as("Expected HTTP status %d but got %d. Body: %s",
                        expectedStatus, context.getLastResponse().statusCode(),
                        context.getLastResponse().body().asString())
                .isEqualTo(expectedStatus);
    }

    @Then("the response should contain a username starting with {string}")
    public void theResponseShouldContainUsername(String prefix) {
        String username = context.getLastResponse().jsonPath().getString("username");
        assertThat(username).startsWith(prefix);
    }

    @Then("the response should contain a JWT token")
    public void theResponseShouldContainJwtToken() {
        String token = context.getLastResponse().jsonPath().getString("token");
        assertThat(token).isNotBlank();
    }

    @Then("the profile firstName should be {string}")
    public void theProfileFirstNameShouldBe(String expectedFirstName) {
        String actual = context.getLastResponse().jsonPath().getString("firstName");
        assertThat(actual).isEqualTo(expectedFirstName);
    }

    @Then("the profile lastName should be {string}")
    public void theProfileLastNameShouldBe(String expectedLastName) {
        String actual = context.getLastResponse().jsonPath().getString("lastName");
        assertThat(actual).isEqualTo(expectedLastName);
    }

    @Then("the trainee profile should no longer be accessible")
    public void theTraineeProfileShouldNoLongerBeAccessible() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .get("/api/trainees/" + context.getRegisteredUsername());
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Then("the response should contain a list of trainers")
    public void theResponseShouldContainListOfTrainers() {
        assertThat(context.getLastResponse().jsonPath().getList("$")).isNotNull();
    }

    @Then("the response should contain a list of trainings")
    public void theResponseShouldContainListOfTrainings() {
        assertThat(context.getLastResponse().jsonPath().getList("$")).isNotNull();
    }
}
