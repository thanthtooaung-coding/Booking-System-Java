package com.alvin.bookingsystem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credit_packages", indexes = {
    @Index(name = "idx_credit_package_country", columnList = "country_id"),
    @Index(name = "idx_credit_package_active", columnList = "active"),
    @Index(name = "idx_credit_package_name_country", columnList = "name,country_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_credit_package_country", columnNames = {"name", "country_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditPackage extends MasterEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_credit_package_country"))
    private Country country;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "validity_days", nullable = false)
    private Integer validityDays;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "creditPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserPackage> userPackages = new ArrayList<>();
}
