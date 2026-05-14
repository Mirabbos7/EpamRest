package org.example.integration;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public class IntegrationCucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    private final IntegrationTestContext context;

    public IntegrationCucumberSpringConfiguration(IntegrationTestContext context) {
        this.context = context;
    }

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        context.reset();
    }
}
