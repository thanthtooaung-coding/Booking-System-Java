package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_packages", indexes = {
    @Index(name = "idx_user_package_user", columnList = "user_id"),
    @Index(name = "idx_user_package_credit_package", columnList = "credit_package_id"),
    @Index(name = "idx_user_package_status", columnList = "status"),
    @Index(name = "idx_user_package_expires", columnList = "expires_at"),
    @Index(name = "idx_user_package_user_status", columnList = "user_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPackage extends MasterEntity {

    public enum Status {
        ACTIVE, EXPIRED, EXHAUSTED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_package_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_package_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_package_credit_package"))
    private CreditPackage creditPackage;

    @Column(name = "remaining_credits", nullable = false)
    @Builder.Default
    private Integer remainingCredits = 0;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "userPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "userPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Waitlist> waitlists = new ArrayList<>();
}
