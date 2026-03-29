package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_user", columnList = "user_id"),
    @Index(name = "idx_booking_class_schedule", columnList = "class_schedule_id"),
    @Index(name = "idx_booking_user_package", columnList = "user_package_id"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_booked_at", columnList = "booked_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_class_schedule", columnNames = {"user_id", "class_schedule_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends MasterEntity {

    public enum Status {
        BOOKED, CHECKED_IN, COMPLETED, CANCELLED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_class_schedule"))
    private ClassSchedule classSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_package_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_user_package"))
    private UserPackage userPackage;

    @Column(name = "credits_used", nullable = false)
    private Integer creditsUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.BOOKED;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "credit_refunded", nullable = false)
    @Builder.Default
    private Boolean creditRefunded = false;
}
