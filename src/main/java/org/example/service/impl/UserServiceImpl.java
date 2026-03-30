package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.User;
import org.example.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public User createUser(String firstName, String lastName) {
        String username = usernameGenerator.generateUsername(
                firstName, lastName,
                u -> userRepository.findByUsername(u).isPresent()
        );
        String password = passwordGenerator.generatePassword();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_USER);
        user.setActive(true);

        log.info("Creating user with username: {}", username);
        return userRepository.save(user);
    }
}