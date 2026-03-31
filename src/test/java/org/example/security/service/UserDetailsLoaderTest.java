package org.example.security.service;

import org.example.entity.User;
import org.example.enums.Role;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsLoaderTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsLoader userDetailsLoader;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Mirabbos");
        user.setLastName("Axmedov");
        user.setUsername("Mirabbos.Axmedov");
        user.setPassword("encoded_password");
        user.setActive(true);
        user.setRole(Role.ROLE_USER);
    }

    @Test
    void loadByUsername_shouldReturnUser() {
        String username = "Mirabbos.Axmedov";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsLoader.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void loadByUsername_shouldThrowException() {
        String username = "Mirabbos.Axmedov";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsLoader.loadUserByUsername(username));
    }
}