package org.example.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.SignInRequest;
import org.example.dto.response.JwtAuthenticationResponse;
import org.example.entity.User;
import org.example.enums.Role;
import org.example.exception.AccountLockedException;
import org.example.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptsService loginAttemptsService;

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        if (loginAttemptsService.isBlocked(request.username())) {
            throw new AccountLockedException("Your account is blocked! Try again in 5 minutes");
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.username(), request.password()));
            loginAttemptsService.loginSucceeded(request.username());
            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            var jwt = jwtTokenService.generateToken(user);
            return new JwtAuthenticationResponse(jwt);
        } catch (AuthenticationException e) {
            loginAttemptsService.loginFailed(request.username());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public void signOut(String authHeader) {
        log.info("User signing out");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtTokenService.invalidateToken(token);
            log.info("Token invalidated successfully");
        } else {
            log.warn("Sign out called with missing or invalid Authorization header");
        }
    }

    @Transactional
    public void promoteToAdmin(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with such id not found!"));
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }
}