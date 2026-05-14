package org.example.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.component.TestContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    private final TestContext context;

    public AuthSteps(TestContext context) {
        this.context = context;
    }

    // ─── GIVEN ──────────────────────────────────────────────────────────────────

    @Given("the user is authenticated")
    public void theUserIsAuthenticated() {
        assertThat(context.getAuthToken()).isNotBlank();
    }

    // ─── WHEN ───────────────────────────────────────────────────────────────────

    @When("I sign in with the registered user credentials")
    public void iSignInWithRegisteredCredentials() {
        Map<String, String> body = new HashMap<>();
        body.put("username", context.getRegisteredUsername());
        body.put("password", context.getRegisteredPassword());

        Response response = RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/auth/sign-in");

        context.setLastResponse(response);
        if (response.statusCode() == 200) {
            context.setAuthToken(response.jsonPath().getString("token"));
        }
    }

    @When("I sign out with the current token")
    public void iSignOutWithCurrentToken() {
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .post("/api/auth/sign-out");

        context.setLastResponse(response);
    }

    @When("I sign in with username and wrong password {string}")
    public void iSignInWithWrongPassword(String wrongPassword) {
        Map<String, String> body = new HashMap<>();
        body.put("username", context.getRegisteredUsername());
        body.put("password", wrongPassword);

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/auth/sign-in"));
    }

    @When("I sign in with username {string} and password {string}")
    public void iSignInWithUsernameAndPassword(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/auth/sign-in"));
    }

    @When("I sign in with blank username")
    public void iSignInWithBlankUsername() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "");
        body.put("password", "somePassword");

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/auth/sign-in"));
    }

    @When("I sign in with blank password")
    public void iSignInWithBlankPassword() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "someone");
        body.put("password", "");

        context.setLastResponse(RestAssured
                .given().contentType(ContentType.JSON).body(body)
                .post("/api/auth/sign-in"));
    }

    @When("I access a protected endpoint without a token")
    public void iAccessProtectedEndpointWithoutToken() {
        context.setLastResponse(RestAssured.given().get("/api/trainees/anybody"));
    }

    @When("I access a protected endpoint with an expired token")
    public void iAccessProtectedEndpointWithExpiredToken() {
        // Deliberately malformed / expired JWT
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9.invalid_signature";

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + expiredToken)
                .get("/api/trainees/test"));
    }
}
