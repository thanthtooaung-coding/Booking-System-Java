package com.alvin.bookingsystem.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "booking:lock:";
    private static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Acquire a distributed lock for booking operations
     * @param classScheduleId The class schedule ID to lock
     * @return Lock identifier if acquired, null otherwise
     */
    public String acquireLock(Long classScheduleId) {
        String lockKey = LOCK_PREFIX + classScheduleId;
        String lockValue = UUID.randomUUID().toString();
        
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey, 
                lockValue, 
                DEFAULT_LOCK_TIMEOUT.toSeconds(), 
                TimeUnit.SECONDS
        );
        
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock for class schedule: {}", classScheduleId);
            return lockValue;
        }
        
        log.debug("Failed to acquire lock for class schedule: {}", classScheduleId);
        return null;
    }

    /**
     * Release a distributed lock
     * @param classScheduleId The class schedule ID
     * @param lockValue The lock identifier returned by acquireLock
     */
    public void releaseLock(Long classScheduleId, String lockValue) {
        String lockKey = LOCK_PREFIX + classScheduleId;

        Object currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null && lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
            log.debug("Released lock for class schedule: {}", classScheduleId);
        } else {
            log.warn("Attempted to release lock that we don't own for class schedule: {}", classScheduleId);
        }
    }

    /**
     * Execute a task with a distributed lock
     * @param classScheduleId The class schedule ID to lock
     * @param task The task to execute
     * @return Result of the task execution
     */
    public <T> T executeWithLock(Long classScheduleId, LockTask<T> task) {
        String lockValue = acquireLock(classScheduleId);
        if (lockValue == null) {
            throw new RuntimeException("Failed to acquire lock for class schedule: " + classScheduleId);
        }
        
        try {
            return task.execute();
        } finally {
            releaseLock(classScheduleId, lockValue);
        }
    }

    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
}
