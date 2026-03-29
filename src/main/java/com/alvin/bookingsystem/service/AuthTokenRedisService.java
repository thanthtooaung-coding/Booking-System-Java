package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.token.EmailVerificationTokenData;
import com.alvin.bookingsystem.dto.token.PasswordResetTokenData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthTokenRedisService {

    private static final String EMAIL_PREFIX = "booking:auth:email-verify:";
    private static final String PWD_PREFIX = "booking:auth:pwd-reset:";
    private static final Duration USED_TOKEN_TTL = Duration.ofDays(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void saveEmailVerification(String token, Long userId, LocalDateTime expiresAt) {
        var data = EmailVerificationTokenData.builder()
                .userId(userId)
                .expiresAt(expiresAt)
                .used(false)
                .verifiedAt(null)
                .build();
        stringRedisTemplate.opsForValue().set(
                EMAIL_PREFIX + token,
                writeJson(data),
                ttlUntilCleanup(expiresAt));
    }

    public Optional<EmailVerificationTokenData> findEmailVerification(String token) {
        return read(stringRedisTemplate.opsForValue().get(EMAIL_PREFIX + token), EmailVerificationTokenData.class);
    }

    public void markEmailVerificationUsed(String token, EmailVerificationTokenData current, LocalDateTime verifiedAt) {
        var updated = EmailVerificationTokenData.builder()
                .userId(current.userId())
                .expiresAt(current.expiresAt())
                .used(true)
                .verifiedAt(verifiedAt)
                .build();
        stringRedisTemplate.opsForValue().set(EMAIL_PREFIX + token, writeJson(updated), USED_TOKEN_TTL);
    }

    public void savePasswordReset(String token, Long userId, LocalDateTime expiresAt) {
        var data = PasswordResetTokenData.builder()
                .userId(userId)
                .expiresAt(expiresAt)
                .used(false)
                .resetAt(null)
                .build();
        stringRedisTemplate.opsForValue().set(
                PWD_PREFIX + token,
                writeJson(data),
                ttlUntilCleanup(expiresAt));
    }

    public Optional<PasswordResetTokenData> findPasswordReset(String token) {
        return read(stringRedisTemplate.opsForValue().get(PWD_PREFIX + token), PasswordResetTokenData.class);
    }

    public void markPasswordResetUsed(String token, PasswordResetTokenData current, LocalDateTime resetAt) {
        var updated = PasswordResetTokenData.builder()
                .userId(current.userId())
                .expiresAt(current.expiresAt())
                .used(true)
                .resetAt(resetAt)
                .build();
        stringRedisTemplate.opsForValue().set(PWD_PREFIX + token, writeJson(updated), USED_TOKEN_TTL);
    }

    private Duration ttlUntilCleanup(LocalDateTime expiresAt) {
        LocalDateTime cleanup = expiresAt.plusDays(7);
        Duration d = Duration.between(LocalDateTime.now(), cleanup);
        if (d.isNegative() || d.isZero()) {
            return Duration.ofDays(7);
        }
        return d;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> Optional<T> read(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
