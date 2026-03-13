package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "training_type")
public class TrainingType extends BaseEntity{

    @Column(name = "training_type_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private TrainingTypeName trainingTypeName;

    public enum TrainingTypeName {
        CARDIO,
        STRENGTH,
        FLEXIBILITY,
        BALANCE,
        OTHER
    }
}