package org.example.component;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    private final TestContext context;

    public CucumberSpringConfiguration(TestContext context) {
        this.context = context;
    }

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        context.reset();
    }
}
