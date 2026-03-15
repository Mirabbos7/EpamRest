package org.example.repository;

import org.example.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);

    boolean existsByUserUsernameAndUserPassword(String username, String password);
    List<Trainer> findAllByUserUsernameIn(List<String> usernames);
}