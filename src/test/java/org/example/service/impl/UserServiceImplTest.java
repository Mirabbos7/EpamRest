package org.example.service.impl;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldReturnSavedUser() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("abc123XYZ");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("John", "Doe");

        assertEquals("John.Doe", result.getUsername());
        assertEquals("abc123XYZ", result.getPassword());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldSetActiveTrue() {
        when(usernameGenerator.generateUsername(eq("Jane"), eq("Smith"), any()))
                .thenReturn("Jane.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("pass999");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("Jane", "Smith");

        assertTrue(result.isActive());
    }

    @Test
    void createUser_shouldCallUsernameGenerator_withCorrectArgs() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser("John", "Doe");

        verify(usernameGenerator).generateUsername(eq("John"), eq("Doe"), any());
    }

    @Test
    void createUser_shouldCallPasswordGenerator() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser("John", "Doe");

        verify(passwordGenerator).generatePassword();
    }

    @Test
    void createUser_shouldHandleDuplicateUsername() {
        when(usernameGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("John.Doe1");
        when(passwordGenerator.generatePassword()).thenReturn("pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("John", "Doe");

        assertEquals("John.Doe1", result.getUsername());
    }
}