package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_schedules", indexes = {
    @Index(name = "idx_class_schedule_class_definition", columnList = "class_definition_id"),
    @Index(name = "idx_class_schedule_country", columnList = "country_id"),
    @Index(name = "idx_class_schedule_datetime", columnList = "class_datetime"),
    @Index(name = "idx_class_schedule_status", columnList = "status"),
    @Index(name = "idx_class_schedule_datetime_status", columnList = "class_datetime,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSchedule extends MasterEntity {

    public enum Status {
        SCHEDULED, ONGOING, COMPLETED, CANCELLED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_definition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_schedule_class_definition"))
    private ClassDefinition classDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_schedule_country"))
    private Country country;

    @Column(name = "class_datetime", nullable = false)
    private LocalDateTime classDateTime;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "booked_slots", nullable = false)
    @Builder.Default
    private Integer bookedSlots = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.SCHEDULED;

    @OneToMany(mappedBy = "classSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "classSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Waitlist> waitlists = new ArrayList<>();
}
