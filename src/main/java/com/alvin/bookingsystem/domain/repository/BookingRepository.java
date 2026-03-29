package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.Booking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends BaseRepository<Booking> {
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.classSchedule.id = :classScheduleId")
    Optional<Booking> findByUserIdAndClassScheduleId(@Param("userId") Long userId, @Param("classScheduleId") Long classScheduleId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.classSchedule.id = :classScheduleId AND b.status IN ('BOOKED', 'CHECKED_IN')")
    Optional<Booking> findActiveBookingByUserIdAndClassScheduleId(@Param("userId") Long userId, @Param("classScheduleId") Long classScheduleId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status IN ('BOOKED', 'CHECKED_IN')")
    List<Booking> findActiveBookingsByUserId(@Param("userId") Long userId);
    
    @Query(value = """
        SELECT b.* FROM bookings b
        JOIN class_schedules cs ON b.class_schedule_id = cs.id
        JOIN class_definitions cd ON cs.class_definition_id = cd.id
        WHERE b.user_id = :userId
          AND b.status IN ('BOOKED', 'CHECKED_IN')
          AND cs.id != :excludeScheduleId
          AND cs.class_datetime < :endTime
          AND (cs.class_datetime + (cd.duration_minutes || ' minutes')::INTERVAL) > :startTime
        """, nativeQuery = true)
    List<Booking> findOverlappingBookings(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeScheduleId") Long excludeScheduleId
    );
}
