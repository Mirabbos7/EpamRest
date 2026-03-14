package org.example.utils;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_hasCorrectLength() {
        String password = passwordGenerator.generatePassword();
        assertThat(password).hasSize(10);
    }

    @Test
    void generatePassword_containsOnlyAllowedCharacters() {
        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String password = passwordGenerator.generatePassword();
        for (char c : password.toCharArray()) {
            assertThat(allowed).contains(String.valueOf(c));
        }
    }

    @Test
    void generatePassword_notBlank() {
        String password = passwordGenerator.generatePassword();
        assertThat(password).isNotBlank();
    }

    @RepeatedTest(5)
    void generatePassword_alwaysLength10() {
        assertThat(passwordGenerator.generatePassword()).hasSize(10);
    }

    @Test
    void generatePassword_isRandom_producesDistinctValues() {
        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            passwords.add(passwordGenerator.generatePassword());
        }
        assertThat(passwords.size()).isGreaterThan(1);
    }
}