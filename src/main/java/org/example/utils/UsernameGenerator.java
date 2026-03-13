package org.example.utils;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class UsernameGenerator {

    public String generateUsername(String firstName, String lastName, Predicate<String> existsChecker){
        String baseUsername = firstName.trim() + "." + lastName.trim();

        if (!existsChecker.test(baseUsername)) {
            return baseUsername;
        }

        int counter = 1;
        String username = baseUsername + counter;

        while(existsChecker.test(username)){
            counter++;
            username = baseUsername + counter;
        }
        return username;
    }
}
