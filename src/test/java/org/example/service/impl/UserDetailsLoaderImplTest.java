package org.example.service.impl;

import org.example.entity.User;
import org.example.enums.Role;
import org.example.repository.UserRepository;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsLoaderImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldReturnSavedUser() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("abc123XYZ");
        when(passwordEncoder.encode("abc123XYZ")).thenReturn("encoded_abc123XYZ");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("John", "Doe");

        assertThat(result.getUsername()).isEqualTo("John.Doe");
        assertThat(result.getPassword()).isEqualTo("encoded_abc123XYZ");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldEncodePassword() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("abc123XYZ");
        when(passwordEncoder.encode("abc123XYZ")).thenReturn("encoded_abc123XYZ");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser("John", "Doe");

        verify(passwordEncoder).encode("abc123XYZ");
    }

    @Test
    void createUser_shouldSetActiveTrue() {
        when(usernameGenerator.generateUsername(eq("Jane"), eq("Smith"), any()))
                .thenReturn("Jane.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("pass999");
        when(passwordEncoder.encode("pass999")).thenReturn("encoded_pass999");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("Jane", "Smith");

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void createUser_shouldCallUsernameGenerator_withCorrectArgs() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser("John", "Doe");

        verify(usernameGenerator).generateUsername(eq("John"), eq("Doe"), any());
    }

    @Test
    void createUser_shouldCallPasswordGenerator() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser("John", "Doe");

        verify(passwordGenerator).generatePassword();
    }

    @Test
    void createUser_shouldHandleDuplicateUsername() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe1");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("John", "Doe");

        assertThat(result.getUsername()).isEqualTo("John.Doe1");
    }
}