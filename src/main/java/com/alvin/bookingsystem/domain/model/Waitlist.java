package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlists", indexes = {
    @Index(name = "idx_waitlist_user", columnList = "user_id"),
    @Index(name = "idx_waitlist_class_schedule", columnList = "class_schedule_id"),
    @Index(name = "idx_waitlist_user_package", columnList = "user_package_id"),
    @Index(name = "idx_waitlist_status", columnList = "status"),
    @Index(name = "idx_waitlist_class_schedule_position", columnList = "class_schedule_id,position")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_waitlist", columnNames = {"user_id", "class_schedule_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist extends MasterEntity {

    public enum Status {
        WAITING, PROMOTED, REFUNDED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_waitlist_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_waitlist_class_schedule"))
    private ClassSchedule classSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_package_id", nullable = false, foreignKey = @ForeignKey(name = "fk_waitlist_user_package"))
    private UserPackage userPackage;

    @Column(name = "credits_reserved", nullable = false)
    private Integer creditsReserved;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.WAITING;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
}
