package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.dto.request.*;
import com.alvin.bookingsystem.dto.response.*;
import com.alvin.bookingsystem.dto.response.LoginResponse;
import com.alvin.bookingsystem.dto.response.UserResponse;
import com.alvin.bookingsystem.dto.response.VerifyEmailResponse;
import com.alvin.bookingsystem.exception.CustomException;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.exception.UnauthorizedException;
import com.alvin.bookingsystem.mapper.UserMapper;
import com.alvin.bookingsystem.service.AuthTokenRedisService;
import com.alvin.bookingsystem.service.UserService;
import com.alvin.bookingsystem.util.JwtUtil;
import com.alvin.bookingsystem.util.MockService;
import com.alvin.bookingsystem.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UserRequest, UserResponse, UserFilter> implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AuthTokenRedisService authTokenRedisService;
    private final JwtUtil jwtUtil;
    private final MockService mockService;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            AuthTokenRedisService authTokenRedisService,
            JwtUtil jwtUtil,
            MockService mockService) {
        super(userRepository);
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.authTokenRedisService = authTokenRedisService;
        this.jwtUtil = jwtUtil;
        this.mockService = mockService;
    }

    @Override
    protected void validateBeforeCreate(UserRequest request) {
        if (request.email() != null && userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEntityException("email", request.email());
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, UserRequest request, User existingEntity) {
        if (request.email() != null && !request.email().equals(existingEntity.getEmail())) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new DuplicateEntityException("email", request.email());
            }
        }
    }

    @Override
    protected User mapRequestToEntity(UserRequest request) {
        return userMapper.toEntity(request);
    }

    @Override
    protected UserResponse mapEntityToResponse(User entity) {
        return userMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(User entity, UserRequest request) {
        userMapper.updateEntity(entity, request);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.USERS);
    }

    @Override
    @Transactional
    public RegisterResponse register(UserRequest request) {
        validateBeforeCreate(request);
        User user = mapRequestToEntity(request);
        
        user.setCreatedById(0L);
        user = userRepository.save(user);
        
        userRepository.updateCreatedById(user.getId(), user.getId());

        String token = jwtUtil.generateVerificationToken();
        authTokenRedisService.saveEmailVerification(token, user.getId(), LocalDateTime.now().plusHours(24));

        try {
            mockService.sendVerifyEmail(user.getEmail(), token, "VERIFICATION");
        } catch (Exception e) {
            
        }

        return RegisterResponse.builder()
                .user(mapEntityToResponse(user))
                .verificationToken(token)
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!PasswordUtil.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getEmailVerified()) {
            throw new CustomException("Email not verified. Please verify your email first.");
        }

        if (!user.getActive()) {
            throw new CustomException("Account is inactive. Please contact support.");
        }

        String token = jwtUtil.generateToken(user);
        UserResponse userResponse = mapEntityToResponse(user);

        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        var emailVerification = authTokenRedisService.findEmailVerification(request.token())
                .orElseThrow(() -> new EntityNotFoundException("Invalid verification token"));

        if (emailVerification.used()) {
            throw new CustomException("Verification token has already been used");
        }

        if (emailVerification.expiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("Verification token has expired");
        }

        User user = userRepository.findById(emailVerification.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found for verification token"));
        user.setEmailVerified(true);
        userRepository.save(user);

        authTokenRedisService.markEmailVerificationUsed(request.token(), emailVerification, LocalDateTime.now());
        evictCrudCacheForId(user.getId());

        return VerifyEmailResponse.builder()
                .email(user.getEmail())
                .verified(true)
                .build();
    }

    @Override
    @Transactional
    public ResendVerificationResponse resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + request.email()));

        if (user.getEmailVerified()) {
            throw new CustomException("Email is already verified");
        }

        String token = jwtUtil.generateVerificationToken();
        authTokenRedisService.saveEmailVerification(token, user.getId(), LocalDateTime.now().plusHours(24));

        try {
            mockService.sendVerifyEmail(user.getEmail(), token, "VERIFICATION");
        } catch (Exception e) {
            throw new CustomException("Failed to send verification email: " + e.getMessage());
        }

        return ResendVerificationResponse.builder()
                .verificationToken(token)
                .build();
    }

    @Override
    public UserResponse getProfile(Long userId) {
        return findById(userId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        validateBeforeUpdate(userId, request, user);
        updateEntityFromRequest(user, request);
        user = userRepository.save(user);
        evictCrudCacheForId(userId);

        return mapEntityToResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!PasswordUtil.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException("Current password is incorrect");
        }

        user.setPassword(PasswordUtil.encode(request.newPassword()));
        userRepository.save(user);
        evictCrudCacheForId(userId);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + request.email()));

        String token = jwtUtil.generateResetToken();
        authTokenRedisService.savePasswordReset(token, user.getId(), LocalDateTime.now().plusHours(1));

        try {
            mockService.sendVerifyEmail(user.getEmail(), token, "PASSWORD_RESET");
        } catch (Exception e) {
            throw new CustomException("Failed to send password reset email: " + e.getMessage());
        }

        return ForgotPasswordResponse.builder()
                .resetToken(token)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var passwordReset = authTokenRedisService.findPasswordReset(request.token())
                .orElseThrow(() -> new EntityNotFoundException("Invalid reset token"));

        if (passwordReset.used()) {
            throw new CustomException("Reset token has already been used");
        }

        if (passwordReset.expiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("Reset token has expired");
        }

        User user = userRepository.findById(passwordReset.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found for reset token"));
        user.setPassword(PasswordUtil.encode(request.newPassword()));
        userRepository.save(user);

        authTokenRedisService.markPasswordResetUsed(request.token(), passwordReset, LocalDateTime.now());
        evictCrudCacheForId(user.getId());
    }
}
