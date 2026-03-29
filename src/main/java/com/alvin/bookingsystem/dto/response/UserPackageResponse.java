package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserPackageResponse(
        Long id,
        Long userId,
        String userEmail,
        Long creditPackageId,
        String creditPackageName,
        Integer remainingCredits,
        LocalDateTime purchasedAt,
        LocalDateTime expiresAt,
        String status,
        MasterData masterData
) {}
