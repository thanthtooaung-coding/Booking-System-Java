package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.Booking;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.repository.BookingRepository;
import com.alvin.bookingsystem.domain.repository.ClassScheduleRepository;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.dto.request.BookingFilter;
import com.alvin.bookingsystem.dto.request.BookingRequest;
import com.alvin.bookingsystem.dto.response.BookingResponse;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.mapper.BookingMapper;
import com.alvin.bookingsystem.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingServiceImpl extends BaseServiceImpl<Booking, BookingRequest, BookingResponse, BookingFilter> implements BookingService {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserPackageRepository userPackageRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              BookingMapper bookingMapper,
                              UserRepository userRepository,
                              ClassScheduleRepository classScheduleRepository,
                              UserPackageRepository userPackageRepository) {
        super(bookingRepository);
        this.bookingMapper = bookingMapper;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userPackageRepository = userPackageRepository;
    }

    @Override
    protected void validateBeforeCreate(BookingRequest request) {
        if (request.userId() != null && request.classScheduleId() != null) {
            if (bookingRepository.findByUserIdAndClassScheduleId(request.userId(), request.classScheduleId()).isPresent()) {
                throw new DuplicateEntityException(
                    "User already has a booking for this class schedule"
                );
            }
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, BookingRequest request, Booking existingEntity) {
        if (request.userId() != null && request.classScheduleId() != null) {
            Long userId = request.userId();
            Long classScheduleId = request.classScheduleId();

            bookingRepository.findByUserIdAndClassScheduleId(userId, classScheduleId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateEntityException(
                            "User already has a booking for this class schedule"
                        );
                    }
                });
        }
    }

    @Override
    protected Booking mapRequestToEntity(BookingRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));
        ClassSchedule classSchedule = classScheduleRepository.findById(request.classScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("ClassSchedule not found with id: " + request.classScheduleId()));
        UserPackage userPackage = userPackageRepository.findById(request.userPackageId())
                .orElseThrow(() -> new EntityNotFoundException("UserPackage not found with id: " + request.userPackageId()));
        return bookingMapper.toEntity(request, user, classSchedule, userPackage);
    }

    @Override
    protected BookingResponse mapEntityToResponse(Booking entity) {
        return bookingMapper.toResponse(entity);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.BOOKINGS);
    }

    @Override
    protected void updateEntityFromRequest(Booking entity, BookingRequest request) {
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
        bookingMapper.updateEntity(entity, request, user, classSchedule, userPackage);
    }
}
