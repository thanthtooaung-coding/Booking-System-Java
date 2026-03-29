package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record VerifyEmailResponse(
        String email,
        Boolean verified
) {}
