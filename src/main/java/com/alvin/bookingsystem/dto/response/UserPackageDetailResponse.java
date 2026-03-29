package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserPackageDetailResponse(
        Long id,
        Long userId,
        String userEmail,
        Long creditPackageId,
        String creditPackageName,
        Long countryId,
        String countryName,
        String countryCode,
        Integer credits,
        Integer remainingCredits,
        LocalDateTime purchasedAt,
        LocalDateTime expiresAt,
        String status,
        MasterData masterData
) {}
