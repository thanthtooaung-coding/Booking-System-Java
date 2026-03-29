package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(
        UserResponse user,
        String verificationToken
) {}
