package org.example.config.service;

import org.example.dto.request.SignInRequest;
import org.example.dto.response.JwtAuthenticationResponse;
import org.example.entity.User;
import org.example.enums.Role;
import org.example.exception.AccountLockedException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LoginAttemptsService loginAttemptsService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john.doe");
        user.setPassword("pass123");
        user.setRole(Role.ROLE_USER);
    }

    @Test
    void signIn_shouldReturnJwt_whenCredentialsAreValid() {
        SignInRequest request = new SignInRequest("john.doe", "pass123");

        when(loginAttemptsService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(user)).thenReturn("jwt.token");

        JwtAuthenticationResponse result = authenticationService.signIn(request);

        assertThat(result.getToken()).isEqualTo("jwt.token");
        verify(loginAttemptsService).loginSucceeded("john.doe");
    }

    @Test
    void signIn_shouldThrowAccountLockedException_whenUserIsBlocked() {
        SignInRequest request = new SignInRequest("john.doe", "pass123");

        when(loginAttemptsService.isBlocked("john.doe")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.signIn(request))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("blocked");

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void signIn_shouldThrowBadCredentialsException_whenAuthenticationFails() {
        SignInRequest request = new SignInRequest("john.doe", "wrongpass");

        when(loginAttemptsService.isBlocked("john.doe")).thenReturn(false);
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authenticationService.signIn(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(loginAttemptsService).loginFailed("john.doe");
    }

    @Test
    void signIn_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        SignInRequest request = new SignInRequest("john.doe", "pass123");

        when(loginAttemptsService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.signIn(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void signOut_shouldInvalidateToken_whenValidHeader() {
        authenticationService.signOut("Bearer valid.jwt.token");

        verify(jwtTokenService).invalidateToken("valid.jwt.token");
    }

    @Test
    void signOut_shouldDoNothing_whenHeaderIsNull() {
        authenticationService.signOut(null);

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void signOut_shouldDoNothing_whenHeaderDoesNotStartWithBearer() {
        authenticationService.signOut("Basic sometoken");

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void promoteToAdmin_shouldSetAdminRole() {
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.promoteToAdmin(1L);

        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void promoteToAdmin_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.promoteToAdmin(99L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with such id not found");
    }
}