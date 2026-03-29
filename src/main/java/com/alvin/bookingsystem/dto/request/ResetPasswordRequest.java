package com.alvin.bookingsystem.dto.request;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}
