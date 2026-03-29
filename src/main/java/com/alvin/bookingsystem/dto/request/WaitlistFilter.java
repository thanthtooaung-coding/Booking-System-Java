package com.alvin.bookingsystem.dto.request;

public record WaitlistFilter(
        Long userId,
        Long classScheduleId,
        String status
) {}
