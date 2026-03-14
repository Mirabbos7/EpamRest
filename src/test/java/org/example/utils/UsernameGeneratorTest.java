package org.example.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorTest {

    UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void generateUsername_noConflict_returnsBase() {
        String username = usernameGenerator.generateUsername("John", "Doe", u -> false);
        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void generateUsername_baseConflict_returnsWithSuffix1() {
        String username = usernameGenerator.generateUsername("John", "Doe", u -> u.equals("John.Doe"));
        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void generateUsername_multipleConflicts_returnsNextAvailable() {
        String username = usernameGenerator.generateUsername("John", "Doe",
                u -> u.equals("John.Doe") || u.equals("John.Doe1"));
        assertThat(username).isEqualTo("John.Doe2");
    }

    @Test
    void generateUsername_manyConflicts_returnsCorrectSuffix() {
        String username = usernameGenerator.generateUsername("John", "Doe",
                u -> u.equals("John.Doe") || u.equals("John.Doe1")
                        || u.equals("John.Doe2") || u.equals("John.Doe3")
                        || u.equals("John.Doe4"));
        assertThat(username).isEqualTo("John.Doe5");
    }

    @Test
    void generateUsername_trimsSpaces() {
        String username = usernameGenerator.generateUsername("  John  ", "  Doe  ", u -> false);
        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void generateUsername_differentNames_noConflict() {
        String username = usernameGenerator.generateUsername("Alice", "Smith", u -> false);
        assertThat(username).isEqualTo("Alice.Smith");
    }

    @Test
    void generateUsername_checkerCalledWithCorrectValues() {
        java.util.List<String> checked = new java.util.ArrayList<>();
        usernameGenerator.generateUsername("John", "Doe", u -> {
            checked.add(u);
            return checked.size() < 3;
        });
        assertThat(checked).containsExactly("John.Doe", "John.Doe1", "John.Doe2");
    }
}