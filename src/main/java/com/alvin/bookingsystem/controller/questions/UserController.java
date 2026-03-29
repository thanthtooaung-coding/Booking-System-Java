package com.alvin.bookingsystem.controller.questions;

import com.alvin.bookingsystem.dto.request.*;
import com.alvin.bookingsystem.dto.response.*;
import com.alvin.bookingsystem.service.UserService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import com.alvin.bookingsystem.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Question Operations - User Module", description = "User Module APIs (Registration, Login, Profile Management)")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user account. After registration, an email verification link will be sent to the user's email address. The user must verify their email before they can use the account."
    )
    public ResponseEntity<ApiResponse> register(@RequestBody UserRequest request, HttpServletRequest httpServletRequest) {
        RegisterResponse response = userService.register(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "User registered successfully. Please verify your email.",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password. Returns JWT access token and refresh token upon successful authentication. The user must have verified their email before logging in."
    )
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        LoginResponse response = userService.login(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Login successful",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verifies a user's email address using the verification token sent to their email. This is required after registration to activate the account."
    )
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody VerifyEmailRequest request, HttpServletRequest httpServletRequest) {
        VerifyEmailResponse response = userService.verifyEmail(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Email verified successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification email",
            description = "Resends the email verification link to the user's email address. Useful if the original verification email was not received or has expired."
    )
    public ResponseEntity<ApiResponse> resendVerification(@RequestBody ResendVerificationRequest request, HttpServletRequest httpServletRequest) {
        ResendVerificationResponse response = userService.resendVerification(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Verification email sent successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information including personal details, email verification status, and account status. Requires authentication."
    )
    public ResponseEntity<ApiResponse> getProfile(HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        UserResponse response = userService.getProfile(userId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Profile retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information such as first name, last name, phone number, etc. Requires authentication."
    )
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody UserRequest request, HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        UserResponse response = userService.updateProfile(userId, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Profile updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password. Requires the current password for verification. Requires authentication."
    )
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        userService.changePassword(userId, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                null,
                "Password changed successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot password",
            description = "Initiates the password reset process by sending a password reset link to the user's email address. No authentication required."
    )
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpServletRequest) {
        ForgotPasswordResponse response = userService.forgotPassword(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Password reset email sent successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using the reset token received via email. The token is sent to the user's email when they request a password reset. No authentication required."
    )
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        userService.resetPassword(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                null,
                "Password reset successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }
}
