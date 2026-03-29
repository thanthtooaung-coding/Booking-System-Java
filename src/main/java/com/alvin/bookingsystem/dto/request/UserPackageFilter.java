package com.alvin.bookingsystem.dto.request;

public record UserPackageFilter(
        Long userId,
        Long creditPackageId,
        String status
) {}
