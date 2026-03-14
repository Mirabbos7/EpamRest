package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    void authenticate_shouldNotThrow_whenCredentialsAreValid() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> true;

        assertDoesNotThrow(() ->
                authService.authenticate("john.doe", "password123", matcher)
        );
    }

    @Test
    void authenticate_shouldThrowSecurityException_whenCredentialsAreInvalid() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> false;

        SecurityException ex = assertThrows(SecurityException.class, () ->
                authService.authenticate("john.doe", "wrongPassword", matcher)
        );

        assertEquals("Invalid credentials: john.doe", ex.getMessage());
    }

    @Test
    void authenticate_shouldIncludeUsernameInExceptionMessage() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> false;
        String username = "jane.smith";

        SecurityException ex = assertThrows(SecurityException.class, () ->
                authService.authenticate(username, "anyPassword", matcher)
        );

        assertTrue(ex.getMessage().contains(username));
    }

    @Test
    void authenticate_shouldPassCorrectArgumentsToMatcher() {
        String expectedUsername = "test.user";
        String expectedPassword = "testPass";

        BiFunction<String, String, Boolean> matcher = (u, p) -> {
            assertEquals(expectedUsername, u);
            assertEquals(expectedPassword, p);
            return true;
        };

        assertDoesNotThrow(() ->
                authService.authenticate(expectedUsername, expectedPassword, matcher)
        );
    }

    @Test
    void authenticate_shouldThrow_whenMatcherReturnsNull() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> null;

        assertThrows(NullPointerException.class, () ->
                authService.authenticate("john.doe", "password", matcher)
        );
    }

    @Test
    void authenticate_shouldThrow_whenUsernameIsEmpty() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> false;

        SecurityException ex = assertThrows(SecurityException.class, () ->
                authService.authenticate("", "password", matcher)
        );

        assertEquals("Invalid credentials: ", ex.getMessage());
    }

    @Test
    void authenticate_shouldNotThrow_whenPasswordIsEmpty_andMatcherReturnsTrue() {
        BiFunction<String, String, Boolean> matcher = (u, p) -> true;

        assertDoesNotThrow(() ->
                authService.authenticate("john.doe", "", matcher)
        );
    }
}