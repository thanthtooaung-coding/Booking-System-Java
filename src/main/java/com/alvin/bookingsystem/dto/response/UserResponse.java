package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Boolean emailVerified,
        Boolean active,
        MasterData masterData
) {}
