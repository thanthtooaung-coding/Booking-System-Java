package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.cache.CrudResponseCache;
import com.alvin.bookingsystem.domain.dao.ScheduleDao;
import com.alvin.bookingsystem.domain.model.*;
import com.alvin.bookingsystem.domain.repository.*;
import com.alvin.bookingsystem.dto.request.AddToWaitlistRequest;
import com.alvin.bookingsystem.dto.request.BookClassRequest;
import com.alvin.bookingsystem.dto.response.*;
import com.alvin.bookingsystem.exception.CustomException;
import com.alvin.bookingsystem.service.ScheduleService;
import com.alvin.bookingsystem.util.RedisLockUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired(required = false)
    private CrudResponseCache crudResponseCache;

    private final ScheduleDao scheduleDao;
    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final RedisLockUtil redisLockUtil;

    private static final int CANCELLATION_REFUND_HOURS = 4;

    private void evictUserPackageCache(Long userPackageId) {
        if (crudResponseCache != null && crudResponseCache.isEnabled()) {
            crudResponseCache.evict(CacheRegions.USER_PACKAGES, userPackageId);
        }
    }

    @Override
    public List<ClassScheduleResponse> getAvailableSchedulesByCountry(Long countryId) {
        return scheduleDao.findAvailableSchedulesByCountryId(countryId);
    }

    @Override
    @Transactional
    public BookClassResponse bookClass(Long userId, BookClassRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        ClassSchedule classSchedule = classScheduleRepository.findById(request.classScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("Class schedule not found with id: " + request.classScheduleId()));

        UserPackage userPackage = userPackageRepository.findById(request.userPackageId())
                .orElseThrow(() -> new EntityNotFoundException("User package not found with id: " + request.userPackageId()));

        // Validate user owns the package
        if (!userPackage.getUser().getId().equals(userId)) {
            throw new CustomException("User package does not belong to the current user");
        }

        // Validate package is active and not expired
        if (userPackage.getStatus() != UserPackage.Status.ACTIVE) {
            throw new CustomException("User package is not active");
        }

        if (userPackage.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("User package has expired");
        }

        // Validate package country matches class country
        if (!userPackage.getCreditPackage().getCountry().getId().equals(classSchedule.getCountry().getId())) {
            throw new CustomException("Package country does not match class country");
        }

        // Validate class schedule is available
        if (classSchedule.getStatus() != ClassSchedule.Status.SCHEDULED) {
            throw new CustomException("Class schedule is not available for booking");
        }

        if (classSchedule.getClassDateTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("Class has already started");
        }

        // Check for overlapping bookings
        ClassDefinition classDefinition = classSchedule.getClassDefinition();
        LocalDateTime classStart = classSchedule.getClassDateTime();
        LocalDateTime classEnd = classStart.plusMinutes(classDefinition.getDurationMinutes());
        
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                userId, classStart, classEnd, classSchedule.getId());
        
        if (!overlappingBookings.isEmpty()) {
            throw new CustomException("You have an overlapping booking at this time");
        }

        // Check if user already has an active booking for this class (not cancelled)
        if (bookingRepository.findActiveBookingByUserIdAndClassScheduleId(userId, request.classScheduleId()).isPresent()) {
            throw new CustomException("You have already booked this class");
        }

        // Check required credits
        if (userPackage.getRemainingCredits() < classDefinition.getRequiredCredits()) {
            throw new CustomException("Insufficient credits. Required: " + classDefinition.getRequiredCredits() + ", Available: " + userPackage.getRemainingCredits());
        }

        // Extract final variables for lambda
        final Long classScheduleId = classSchedule.getId();
        final Long finalUserId = userId;
        final Long finalUserPackageId = userPackage.getId();
        final User finalUser = user;
        final ClassDefinition finalClassDefinition = classDefinition;
        final Long finalRequestClassScheduleId = request.classScheduleId();
        final Integer requiredCredits = classDefinition.getRequiredCredits();

        // Use Redis lock for concurrent booking prevention
        return redisLockUtil.executeWithLock(classScheduleId, () -> {
            // Re-fetch to get latest booked_slots
            ClassSchedule currentClassSchedule = classScheduleRepository.findById(finalRequestClassScheduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Class schedule not found"));

            // Check if class is full
            if (currentClassSchedule.getBookedSlots() >= currentClassSchedule.getMaxSlots()) {
                throw new CustomException("Class is full. Please add to waitlist instead");
            }

            // Re-fetch user package to ensure we have latest state
            UserPackage currentUserPackage = userPackageRepository.findById(finalUserPackageId)
                    .orElseThrow(() -> new EntityNotFoundException("User package not found"));

            // Create booking
            Booking booking = Booking.builder()
                    .user(finalUser)
                    .classSchedule(currentClassSchedule)
                    .userPackage(currentUserPackage)
                    .creditsUsed(requiredCredits)
                    .status(Booking.Status.BOOKED)
                    .bookedAt(LocalDateTime.now())
                    .creditRefunded(false)
                    .build();

            booking.setCreatedById(finalUserId);
            booking = bookingRepository.save(booking);

            // Deduct credits
            currentUserPackage.setRemainingCredits(currentUserPackage.getRemainingCredits() - requiredCredits);
            userPackageRepository.save(currentUserPackage);
            evictUserPackageCache(currentUserPackage.getId());

            // Update booked slots
            currentClassSchedule.setBookedSlots(currentClassSchedule.getBookedSlots() + 1);
            classScheduleRepository.save(currentClassSchedule);

            log.info("User {} booked class schedule {}", finalUserId, finalRequestClassScheduleId);

            return BookClassResponse.builder()
                    .bookingId(booking.getId())
                    .classScheduleId(currentClassSchedule.getId())
                    .className(finalClassDefinition.getName())
                    .classDateTime(currentClassSchedule.getClassDateTime())
                    .creditsUsed(requiredCredits)
                    .remainingCredits(currentUserPackage.getRemainingCredits())
                    .status(booking.getStatus().name())
                    .bookedAt(booking.getBookedAt())
                    .build();
        });
    }

    @Override
    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomException("Booking does not belong to the current user");
        }

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new CustomException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.Status.COMPLETED) {
            throw new CustomException("Cannot cancel a completed booking");
        }

        ClassSchedule classSchedule = booking.getClassSchedule();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = classSchedule.getClassDateTime();
        boolean shouldRefund = classStart.isAfter(now.plusHours(CANCELLATION_REFUND_HOURS));

        redisLockUtil.executeWithLock(classSchedule.getId(), () -> {
            booking.setStatus(Booking.Status.CANCELLED);
            booking.setCancelledAt(now);
            bookingRepository.save(booking);

            if (shouldRefund && !booking.getCreditRefunded()) {
                UserPackage userPackage = booking.getUserPackage();
                userPackage.setRemainingCredits(userPackage.getRemainingCredits() + booking.getCreditsUsed());
                userPackageRepository.save(userPackage);
                evictUserPackageCache(userPackage.getId());
                booking.setCreditRefunded(true);
                bookingRepository.save(booking);
                log.info("Credits refunded for cancelled booking {}", bookingId);
            }

            classSchedule.setBookedSlots(Math.max(0, classSchedule.getBookedSlots() - 1));
            classScheduleRepository.save(classSchedule);

            promoteWaitlist(classSchedule);

            log.info("User {} cancelled booking {}", userId, bookingId);
            return null;
        });
    }

    @Override
    @Transactional
    public WaitlistResponse addToWaitlist(Long userId, AddToWaitlistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        ClassSchedule classSchedule = classScheduleRepository.findById(request.classScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("Class schedule not found with id: " + request.classScheduleId()));

        UserPackage userPackage = userPackageRepository.findById(request.userPackageId())
                .orElseThrow(() -> new EntityNotFoundException("User package not found with id: " + request.userPackageId()));

        if (!userPackage.getUser().getId().equals(userId)) {
            throw new CustomException("User package does not belong to the current user");
        }

        if (userPackage.getStatus() != UserPackage.Status.ACTIVE) {
            throw new CustomException("User package is not active");
        }

        if (userPackage.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("User package has expired");
        }

        if (!userPackage.getCreditPackage().getCountry().getId().equals(classSchedule.getCountry().getId())) {
            throw new CustomException("Package country does not match class country");
        }

        if (classSchedule.getStatus() != ClassSchedule.Status.SCHEDULED) {
            throw new CustomException("Class schedule is not available for waitlist");
        }

        if (classSchedule.getClassDateTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("Class has already started");
        }

        // Check if already in active waitlist (WAITING status only, not PROMOTED or REFUNDED)
        if (waitlistRepository.findActiveWaitlistByUserIdAndClassScheduleId(userId, request.classScheduleId()).isPresent()) {
            throw new CustomException("You are already in the waitlist for this class");
        }

        // Check if already has an active booking for this class (not cancelled)
        if (bookingRepository.findActiveBookingByUserIdAndClassScheduleId(userId, request.classScheduleId()).isPresent()) {
            throw new CustomException("You have already booked this class");
        }

        ClassDefinition classDefinition = classSchedule.getClassDefinition();

        if (userPackage.getRemainingCredits() < classDefinition.getRequiredCredits()) {
            throw new CustomException("Insufficient credits. Required: " + classDefinition.getRequiredCredits());
        }

        final Long classScheduleId = classSchedule.getId();
        final Long finalUserId = userId;
        final Long finalUserPackageId = userPackage.getId();
        final User finalUser = user;
        final Long finalRequestClassScheduleId = request.classScheduleId();
        final Integer requiredCredits = classDefinition.getRequiredCredits();

        return redisLockUtil.executeWithLock(classScheduleId, () -> {
            ClassSchedule currentClassSchedule = classScheduleRepository.findById(finalRequestClassScheduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Class schedule not found"));

            if (currentClassSchedule.getBookedSlots() < currentClassSchedule.getMaxSlots()) {
                throw new CustomException("Class has available slots. Please book directly instead");
            }

            Integer maxPosition = waitlistRepository.findMaxPositionByClassScheduleId(currentClassSchedule.getId());
            int nextPosition = (maxPosition != null ? maxPosition : 0) + 1;

            UserPackage currentUserPackage = userPackageRepository.findById(finalUserPackageId)
                    .orElseThrow(() -> new EntityNotFoundException("User package not found"));

            Waitlist waitlist = Waitlist.builder()
                    .user(finalUser)
                    .classSchedule(currentClassSchedule)
                    .userPackage(currentUserPackage)
                    .creditsReserved(requiredCredits)
                    .position(nextPosition)
                    .status(Waitlist.Status.WAITING)
                    .joinedAt(LocalDateTime.now())
                    .build();

            waitlist.setCreatedById(finalUserId);
            waitlist = waitlistRepository.save(waitlist);

            if (currentUserPackage.getRemainingCredits() < requiredCredits) {
                throw new CustomException("Insufficient credits. Required: " + requiredCredits + ", Available: " + currentUserPackage.getRemainingCredits());
            }
            currentUserPackage.setRemainingCredits(currentUserPackage.getRemainingCredits() - requiredCredits);
            userPackageRepository.save(currentUserPackage);
            evictUserPackageCache(currentUserPackage.getId());

            log.info("User {} added to waitlist for class schedule {} at position {}", finalUserId, finalRequestClassScheduleId, nextPosition);

            return WaitlistResponse.builder()
                    .id(waitlist.getId())
                    .userId(waitlist.getUser().getId())
                    .userEmail(waitlist.getUser().getEmail())
                    .classScheduleId(waitlist.getClassSchedule().getId())
                    .className(waitlist.getClassSchedule().getClassDefinition().getName())
                    .classDateTime(waitlist.getClassSchedule().getClassDateTime())
                    .userPackageId(waitlist.getUserPackage().getId())
                    .packageName(waitlist.getUserPackage().getCreditPackage().getName())
                    .creditsReserved(waitlist.getCreditsReserved())
                    .position(waitlist.getPosition())
                    .status(waitlist.getStatus().name())
                    .joinedAt(waitlist.getJoinedAt())
                    .promotedAt(waitlist.getPromotedAt())
                    .refundedAt(waitlist.getRefundedAt())
                    .masterData(MasterData.builder()
                            .id(waitlist.getId())
                            .createdBy(waitlist.getCreatedById())
                            .updatedBy(waitlist.getUpdatedById())
                            .createdAt(waitlist.getCreatedAt())
                            .updatedAt(waitlist.getUpdatedAt())
                            .build())
                    .build();
        });
    }

    @Override
    @Transactional
    public void checkIn(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomException("Booking does not belong to the current user");
        }

        if (booking.getStatus() != Booking.Status.BOOKED) {
            throw new CustomException("Only booked classes can be checked in");
        }

        ClassSchedule classSchedule = booking.getClassSchedule();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = classSchedule.getClassDateTime();

        if (classStart.isAfter(now.plusMinutes(15))) {
            throw new CustomException("Check-in is only available 15 minutes before class starts");
        }

        if (classStart.plusMinutes(classSchedule.getClassDefinition().getDurationMinutes()).isBefore(now)) {
            throw new CustomException("Class has already ended");
        }

        booking.setStatus(Booking.Status.CHECKED_IN);
        booking.setCheckedInAt(now);
        bookingRepository.save(booking);

        log.info("User {} checked in to booking {}", userId, bookingId);
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<Booking> bookings = bookingRepository.findActiveBookingsByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WaitlistResponse> getUserWaitlists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<Waitlist> waitlists = waitlistRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(userId) && w.getStatus() == Waitlist.Status.WAITING)
                .toList();

        return waitlists.stream()
                .map(this::mapToWaitlistResponse)
                .collect(Collectors.toList());
    }

    /**
     * Promote the first waitlist entry to booking when a slot becomes available
     */
    @Transactional
    protected void promoteWaitlist(ClassSchedule classSchedule) {
        if (classSchedule.getBookedSlots() >= classSchedule.getMaxSlots()) {
            return; // Still full
        }

        List<Waitlist> waitingEntries = waitlistRepository.findWaitingEntriesByClassScheduleIdOrderByPosition(classSchedule.getId());
        if (waitingEntries.isEmpty()) {
            return; // No one waiting
        }

        Waitlist firstWaitlist = waitingEntries.getFirst();

        UserPackage userPackage = firstWaitlist.getUserPackage();

        Booking booking = Booking.builder()
                .user(firstWaitlist.getUser())
                .classSchedule(classSchedule)
                .userPackage(userPackage)
                .creditsUsed(firstWaitlist.getCreditsReserved())
                .status(Booking.Status.BOOKED)
                .bookedAt(LocalDateTime.now())
                .creditRefunded(false)
                .build();

        booking.setCreatedById(firstWaitlist.getUser().getId());
        booking = bookingRepository.save(booking);

        firstWaitlist.setStatus(Waitlist.Status.PROMOTED);
        firstWaitlist.setPromotedAt(LocalDateTime.now());
        waitlistRepository.save(firstWaitlist);

        classSchedule.setBookedSlots(classSchedule.getBookedSlots() + 1);
        classScheduleRepository.save(classSchedule);

        log.info("Promoted waitlist entry {} to booking {} for class schedule {}", 
                firstWaitlist.getId(), booking.getId(), classSchedule.getId());
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .classScheduleId(booking.getClassSchedule().getId())
                .className(booking.getClassSchedule().getClassDefinition().getName())
                .classDateTime(booking.getClassSchedule().getClassDateTime())
                .userPackageId(booking.getUserPackage().getId())
                .packageName(booking.getUserPackage().getCreditPackage().getName())
                .creditsUsed(booking.getCreditsUsed())
                .status(booking.getStatus().name())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .checkedInAt(booking.getCheckedInAt())
                .creditRefunded(booking.getCreditRefunded())
                .masterData(MasterData.builder()
                        .id(booking.getId())
                        .createdBy(booking.getCreatedById())
                        .updatedBy(booking.getUpdatedById())
                        .createdAt(booking.getCreatedAt())
                        .updatedAt(booking.getUpdatedAt())
                        .build())
                .build();
    }

    private WaitlistResponse mapToWaitlistResponse(Waitlist waitlist) {
        return WaitlistResponse.builder()
                .id(waitlist.getId())
                .userId(waitlist.getUser().getId())
                .userEmail(waitlist.getUser().getEmail())
                .classScheduleId(waitlist.getClassSchedule().getId())
                .className(waitlist.getClassSchedule().getClassDefinition().getName())
                .classDateTime(waitlist.getClassSchedule().getClassDateTime())
                .userPackageId(waitlist.getUserPackage().getId())
                .packageName(waitlist.getUserPackage().getCreditPackage().getName())
                .creditsReserved(waitlist.getCreditsReserved())
                .position(waitlist.getPosition())
                .status(waitlist.getStatus().name())
                .joinedAt(waitlist.getJoinedAt())
                .promotedAt(waitlist.getPromotedAt())
                .refundedAt(waitlist.getRefundedAt())
                .masterData(MasterData.builder()
                        .id(waitlist.getId())
                        .createdBy(waitlist.getCreatedById())
                        .updatedBy(waitlist.getUpdatedById())
                        .createdAt(waitlist.getCreatedAt())
                        .updatedAt(waitlist.getUpdatedAt())
                        .build())
                .build();
    }
}
