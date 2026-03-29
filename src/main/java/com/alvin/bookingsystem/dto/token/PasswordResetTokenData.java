package com.alvin.bookingsystem.dto.token;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PasswordResetTokenData(
        Long userId,
        LocalDateTime expiresAt,
        boolean used,
        LocalDateTime resetAt
) {}
