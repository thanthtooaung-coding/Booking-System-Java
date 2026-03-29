package com.alvin.bookingsystem.dto.request;

import java.time.LocalDateTime;

public record UserPackageRequest(
        Long userId,
        Long creditPackageId,
        LocalDateTime purchasedAt,
        LocalDateTime expiresAt
) {}
