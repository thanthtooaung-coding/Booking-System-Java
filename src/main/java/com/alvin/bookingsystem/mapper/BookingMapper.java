package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.Booking;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.dto.request.BookingRequest;
import com.alvin.bookingsystem.dto.response.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final MasterDataMapper masterDataMapper;

    public Booking toEntity(BookingRequest request, User user, ClassSchedule classSchedule, UserPackage userPackage) {
        Booking.Status status = request.status() != null
                ? Booking.Status.valueOf(request.status())
                : Booking.Status.BOOKED;

        return Booking.builder()
                .user(user)
                .classSchedule(classSchedule)
                .userPackage(userPackage)
                .creditsUsed(request.creditsUsed())
                .status(status)
                .bookedAt(request.bookedAt() != null ? request.bookedAt() : java.time.LocalDateTime.now())
                .creditRefunded(false)
                .build();
    }

    public BookingResponse toResponse(Booking entity) {
        if (entity == null) {
            return null;
        }

        String userEmail = null;
        if (entity.getUser() != null) {
            userEmail = entity.getUser().getEmail();
        }

        return BookingResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(userEmail)
                .classScheduleId(entity.getClassSchedule() != null ? entity.getClassSchedule().getId() : null)
                .userPackageId(entity.getUserPackage() != null ? entity.getUserPackage().getId() : null)
                .creditsUsed(entity.getCreditsUsed())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .bookedAt(entity.getBookedAt())
                .cancelledAt(entity.getCancelledAt())
                .checkedInAt(entity.getCheckedInAt())
                .creditRefunded(entity.getCreditRefunded())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Booking entity, BookingRequest request, User user, ClassSchedule classSchedule, UserPackage userPackage) {
        if (user != null) {
            entity.setUser(user);
        }
        if (classSchedule != null) {
            entity.setClassSchedule(classSchedule);
        }
        if (userPackage != null) {
            entity.setUserPackage(userPackage);
        }
        if (request.creditsUsed() != null) {
            entity.setCreditsUsed(request.creditsUsed());
        }
        if (request.status() != null) {
            entity.setStatus(Booking.Status.valueOf(request.status()));
        }
        if (request.bookedAt() != null) {
            entity.setBookedAt(request.bookedAt());
        }
    }
}
