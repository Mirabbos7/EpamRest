package org.example.config.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptsServiceTest {

    private LoginAttemptsService loginAttemptsService;

    @BeforeEach
    void setUp() {
        loginAttemptsService = new LoginAttemptsService();
    }

    @Test
    void isBlocked_shouldReturnFalse_whenNoAttempts() {
        assertThat(loginAttemptsService.isBlocked("john.doe")).isFalse();
    }

    @Test
    void isBlocked_shouldReturnFalse_whenLessThanMaxAttempts() {
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");

        assertThat(loginAttemptsService.isBlocked("john.doe")).isFalse();
    }

    @Test
    void isBlocked_shouldReturnTrue_whenMaxAttemptsReached() {
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");

        assertThat(loginAttemptsService.isBlocked("john.doe")).isTrue();
    }

    @Test
    void loginSucceeded_shouldUnblockUser() {
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");

        loginAttemptsService.loginSucceeded("john.doe");

        assertThat(loginAttemptsService.isBlocked("john.doe")).isFalse();
    }

    @Test
    void loginFailed_shouldNotBlockUser_whenOnlyTwoAttempts() {
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");

        assertThat(loginAttemptsService.isBlocked("john.doe")).isFalse();
    }

    @Test
    void isBlocked_shouldTrackDifferentUsersSeparately() {
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");
        loginAttemptsService.loginFailed("john.doe");

        assertThat(loginAttemptsService.isBlocked("john.doe")).isTrue();
        assertThat(loginAttemptsService.isBlocked("jane.smith")).isFalse();
    }
}