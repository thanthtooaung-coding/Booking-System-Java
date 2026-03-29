package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PurchasePackageResponse(
        Long id,
        Long userId,
        Long creditPackageId,
        String creditPackageName,
        Integer credits,
        Integer remainingCredits,
        LocalDateTime purchasedAt,
        LocalDateTime expiresAt,
        String status
) {}
