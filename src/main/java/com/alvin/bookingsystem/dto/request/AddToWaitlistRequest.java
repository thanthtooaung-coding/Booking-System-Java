package com.alvin.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddToWaitlistRequest(
        @NotNull(message = "Class schedule ID cannot be null")
        Long classScheduleId,
        
        @NotNull(message = "User package ID cannot be null")
        Long userPackageId
) {}
