package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record ApiResponse(
        int success,
        int code,
        Object meta,
        Object data,
        String message
) {}
