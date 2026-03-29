package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/*
* This entity is used to store the definition of a class.
*/
@Entity
@Table(name = "class_definitions", indexes = {
    @Index(name = "idx_class_definition_country", columnList = "country_id"),
    @Index(name = "idx_class_definition_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDefinition extends MasterEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_definition_country"))
    private Country country;

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "instructor_name", length = 100)
    private String instructorName;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "classDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClassSchedule> classSchedules = new ArrayList<>();
}
