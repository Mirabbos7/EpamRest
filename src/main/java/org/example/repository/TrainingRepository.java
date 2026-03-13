package org.example.repository;

import org.example.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>,
        JpaSpecificationExecutor<Training> {

    List<Training> findByTraineeUserUsername(String username);

    List<Training> findByTrainerUserUsername(String username);
}