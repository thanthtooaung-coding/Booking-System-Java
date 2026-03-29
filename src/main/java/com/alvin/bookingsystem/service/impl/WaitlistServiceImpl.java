package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.model.Waitlist;
import com.alvin.bookingsystem.domain.repository.ClassScheduleRepository;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.domain.repository.WaitlistRepository;
import com.alvin.bookingsystem.dto.request.WaitlistFilter;
import com.alvin.bookingsystem.dto.request.WaitlistRequest;
import com.alvin.bookingsystem.dto.response.WaitlistResponse;
import com.alvin.bookingsystem.exception.CustomException;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.mapper.WaitlistMapper;
import com.alvin.bookingsystem.service.WaitlistService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WaitlistServiceImpl extends BaseServiceImpl<Waitlist, WaitlistRequest, WaitlistResponse, WaitlistFilter> implements WaitlistService {

    private final WaitlistMapper waitlistMapper;
    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserPackageRepository userPackageRepository;

    public WaitlistServiceImpl(WaitlistRepository waitlistRepository,
                               WaitlistMapper waitlistMapper,
                               UserRepository userRepository,
                               ClassScheduleRepository classScheduleRepository,
                               UserPackageRepository userPackageRepository) {
        super(waitlistRepository);
        this.waitlistMapper = waitlistMapper;
        this.waitlistRepository = waitlistRepository;
        this.userRepository = userRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userPackageRepository = userPackageRepository;
    }

    @Override
    protected void validateBeforeCreate(WaitlistRequest request) {
        if (request.userId() != null && request.classScheduleId() != null) {
            if (waitlistRepository.findByUserIdAndClassScheduleId(request.userId(), request.classScheduleId()).isPresent()) {
                throw new DuplicateEntityException(
                    "User already has a waitlist entry for this class schedule"
                );
            }
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, WaitlistRequest request, Waitlist existingEntity) {
        if (request.userId() != null && request.classScheduleId() != null) {
            Long userId = request.userId();
            Long classScheduleId = request.classScheduleId();

            waitlistRepository.findByUserIdAndClassScheduleId(userId, classScheduleId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateEntityException(
                            "User already has a waitlist entry for this class schedule"
                        );
                    }
                });
        }
    }

    @Override
    protected Waitlist mapRequestToEntity(WaitlistRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));
        ClassSchedule classSchedule = classScheduleRepository.findById(request.classScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("ClassSchedule not found with id: " + request.classScheduleId()));
        UserPackage userPackage = userPackageRepository.findById(request.userPackageId())
                .orElseThrow(() -> new EntityNotFoundException("UserPackage not found with id: " + request.userPackageId()));
        return waitlistMapper.toEntity(request, user, classSchedule, userPackage);
    }

    @Override
    protected WaitlistResponse mapEntityToResponse(Waitlist entity) {
        return waitlistMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Waitlist entity, WaitlistRequest request) {
        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));
        }
        ClassSchedule classSchedule = null;
        if (request.classScheduleId() != null) {
            classSchedule = classScheduleRepository.findById(request.classScheduleId())
                    .orElseThrow(() -> new EntityNotFoundException("ClassSchedule not found with id: " + request.classScheduleId()));
        }
        UserPackage userPackage = null;
        if (request.userPackageId() != null) {
            userPackage = userPackageRepository.findById(request.userPackageId())
                    .orElseThrow(() -> new EntityNotFoundException("UserPackage not found with id: " + request.userPackageId()));
        }
        waitlistMapper.updateEntity(entity, request, user, classSchedule, userPackage);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.WAITLISTS);
    }

    /**
     * Align CRUD creates with schedule API: credits are deducted when joining the waitlist (WAITING).
     */
    @Override
    protected void beforePersistOnCreate(Waitlist entity) {
        if (entity.getStatus() != Waitlist.Status.WAITING) {
            return;
        }
        UserPackage up = userPackageRepository.findById(entity.getUserPackage().getId())
                .orElseThrow(() -> new EntityNotFoundException("UserPackage not found with id: " + entity.getUserPackage().getId()));
        int reserved = entity.getCreditsReserved();
        if (reserved <= 0) {
            throw new CustomException("creditsReserved must be positive");
        }
        if (up.getRemainingCredits() < reserved) {
            throw new CustomException("Insufficient credits. Required: " + reserved + ", Available: " + up.getRemainingCredits());
        }
        up.setRemainingCredits(up.getRemainingCredits() - reserved);
        userPackageRepository.save(up);
        evictCrudCacheRegion(CacheRegions.USER_PACKAGES, up.getId());
    }

    /**
     * Refund reserved credits if a WAITING entry is removed (admin delete).
     */
    @Override
    protected void beforeDelete(Waitlist entity) {
        if (entity.getStatus() != Waitlist.Status.WAITING) {
            return;
        }
        UserPackage up = userPackageRepository.findById(entity.getUserPackage().getId())
                .orElseThrow(() -> new EntityNotFoundException("UserPackage not found with id: " + entity.getUserPackage().getId()));
        up.setRemainingCredits(up.getRemainingCredits() + entity.getCreditsReserved());
        userPackageRepository.save(up);
        evictCrudCacheRegion(CacheRegions.USER_PACKAGES, up.getId());
    }
}
