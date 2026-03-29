package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.model.Waitlist;
import com.alvin.bookingsystem.dto.request.WaitlistRequest;
import com.alvin.bookingsystem.dto.response.WaitlistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitlistMapper {

    private final MasterDataMapper masterDataMapper;

    public Waitlist toEntity(WaitlistRequest request, User user, ClassSchedule classSchedule, UserPackage userPackage) {
        Waitlist.Status status = request.status() != null
                ? Waitlist.Status.valueOf(request.status())
                : Waitlist.Status.WAITING;

        return Waitlist.builder()
                .user(user)
                .classSchedule(classSchedule)
                .userPackage(userPackage)
                .creditsReserved(request.creditsReserved())
                .position(request.position())
                .status(status)
                .joinedAt(request.joinedAt() != null ? request.joinedAt() : java.time.LocalDateTime.now())
                .build();
    }

    public WaitlistResponse toResponse(Waitlist entity) {
        if (entity == null) {
            return null;
        }

        String userEmail = null;
        if (entity.getUser() != null) {
            userEmail = entity.getUser().getEmail();
        }

        return WaitlistResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(userEmail)
                .classScheduleId(entity.getClassSchedule() != null ? entity.getClassSchedule().getId() : null)
                .userPackageId(entity.getUserPackage() != null ? entity.getUserPackage().getId() : null)
                .creditsReserved(entity.getCreditsReserved())
                .position(entity.getPosition())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .joinedAt(entity.getJoinedAt())
                .promotedAt(entity.getPromotedAt())
                .refundedAt(entity.getRefundedAt())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Waitlist entity, WaitlistRequest request, User user, ClassSchedule classSchedule, UserPackage userPackage) {
        if (user != null) {
            entity.setUser(user);
        }
        if (classSchedule != null) {
            entity.setClassSchedule(classSchedule);
        }
        if (userPackage != null) {
            entity.setUserPackage(userPackage);
        }
        if (request.creditsReserved() != null) {
            entity.setCreditsReserved(request.creditsReserved());
        }
        if (request.position() != null) {
            entity.setPosition(request.position());
        }
        if (request.status() != null) {
            entity.setStatus(Waitlist.Status.valueOf(request.status()));
        }
        if (request.joinedAt() != null) {
            entity.setJoinedAt(request.joinedAt());
        }
    }
}
