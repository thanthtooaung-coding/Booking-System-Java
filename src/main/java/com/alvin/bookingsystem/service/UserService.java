package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.*;
import com.alvin.bookingsystem.dto.response.*;
import com.alvin.bookingsystem.dto.response.LoginResponse;
import com.alvin.bookingsystem.dto.response.UserResponse;
import com.alvin.bookingsystem.dto.response.VerifyEmailResponse;

public interface UserService extends BaseService<UserRequest, UserResponse, UserFilter> {
    RegisterResponse register(UserRequest request);
    LoginResponse login(LoginRequest request);
    VerifyEmailResponse verifyEmail(VerifyEmailRequest request);
    ResendVerificationResponse resendVerification(ResendVerificationRequest request);
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UserRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
