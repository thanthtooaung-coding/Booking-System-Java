package com.alvin.bookingsystem.dto.request;

public record BookingFilter(
        Long userId,
        Long classScheduleId,
        String status
) {}
