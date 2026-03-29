package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record ResendVerificationResponse(
        String verificationToken
) {}
