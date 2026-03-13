package org.example.service;

import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
public class AuthService {
    public void authenticate(String username, String password,
                             BiFunction<String, String, Boolean> matcher) {
        if (!matcher.apply(username, password)) {
            throw new SecurityException("Invalid credentials: " + username);
        }
    }
}