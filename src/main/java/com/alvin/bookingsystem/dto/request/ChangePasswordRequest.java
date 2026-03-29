package com.alvin.bookingsystem.dto.request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}
