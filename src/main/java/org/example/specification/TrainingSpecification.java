package org.example.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.example.entity.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;

public class TrainingSpecification {
    private static Specification<Training> fromDate(Date from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("date"), from);
        };
    }

    private static Specification<Training> toDate(Date to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("date"), to);
        };
    }

    private static Specification<Training> hasTraineeUsername(String username) {
        return (root, query, cb) -> {
            if (username == null) return null;
            Join<Training, Trainee> traineeJoin = root.join("trainee", JoinType.INNER);
            Join<Trainee, User> userJoin = traineeJoin.join("user", JoinType.INNER);
            return cb.equal(userJoin.get("username"), username);
        };
    }

    private static Specification<Training> hasTrainerUsername(String username) {
        return (root, query, cb) -> {
            if (username == null) return null;
            Join<Training, Trainer> trainerJoin = root.join("trainer", JoinType.INNER);
            Join<Trainer, User> userJoin = trainerJoin.join("user", JoinType.INNER);
            return cb.equal(userJoin.get("username"), username);
        };
    }

    private static Specification<Training> hasTrainingType(TrainingType.TrainingTypeName typeName) {
        return (root, query, cb) -> {
            if (typeName == null) return null;
            Join<Training, TrainingType> typeJoin = root.join("trainingType", JoinType.INNER);
            return cb.equal(typeJoin.get("trainingTypeName"), typeName);
        };
    }

    private static Specification<Training> hasTraineeIds(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) return null;
            Join<Training, Trainee> traineeJoin = root.join("trainee", JoinType.INNER);
            return traineeJoin.get("id").in(ids);
        };
    }

    public static Specification<Training> byTraineeCriteria(
            String traineeUsername,
            Date from,
            Date to,
            String trainerUsername,
            TrainingType.TrainingTypeName trainingTypeName) {

        return Specification
                .where(hasTraineeUsername(traineeUsername))
                .and(fromDate(from))
                .and(toDate(to))
                .and(hasTrainerUsername(trainerUsername))
                .and(hasTrainingType(trainingTypeName));
    }

    public static Specification<Training> byTrainerCriteria(
            String trainerUsername,
            Date from,
            Date to,
            String traineeUsername) {

        return Specification
                .where(hasTrainerUsername(trainerUsername))
                .and(fromDate(from))
                .and(toDate(to))
                .and(hasTraineeUsername(traineeUsername));
    }

    public static Specification<Training> forTraineesNextWeek(List<Long> traineeIds) {
        LocalDate nextMonday = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate nextSunday = nextMonday.plusDays(6);

        Date from = Date.from(nextMonday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date to = Date.from(nextSunday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return Specification
                .where(hasTraineeIds(traineeIds))
                .and(fromDate(from))
                .and(toDate(to));
    }
}
