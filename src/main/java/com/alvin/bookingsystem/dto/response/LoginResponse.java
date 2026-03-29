package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        String token,
        UserResponse user
) {}
