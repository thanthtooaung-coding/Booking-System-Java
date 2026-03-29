package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.Waitlist;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends BaseRepository<Waitlist> {
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId AND w.classSchedule.id = :classScheduleId")
    Optional<Waitlist> findByUserIdAndClassScheduleId(@Param("userId") Long userId, @Param("classScheduleId") Long classScheduleId);
    
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId AND w.classSchedule.id = :classScheduleId AND w.status = 'WAITING'")
    Optional<Waitlist> findActiveWaitlistByUserIdAndClassScheduleId(@Param("userId") Long userId, @Param("classScheduleId") Long classScheduleId);
    
    @Query("SELECT w FROM Waitlist w WHERE w.classSchedule.id = :classScheduleId AND w.status = 'WAITING' ORDER BY w.position ASC")
    List<Waitlist> findWaitingEntriesByClassScheduleIdOrderByPosition(@Param("classScheduleId") Long classScheduleId);
    
    @Query("SELECT COALESCE(MAX(w.position), 0) FROM Waitlist w WHERE w.classSchedule.id = :classScheduleId")
    Integer findMaxPositionByClassScheduleId(@Param("classScheduleId") Long classScheduleId);
    
    @Query(value = """
        SELECT w.* FROM waitlists w
        JOIN class_schedules cs ON w.class_schedule_id = cs.id
        JOIN class_definitions cd ON cs.class_definition_id = cd.id
        WHERE w.status = 'WAITING'
          AND (cs.class_datetime + (cd.duration_minutes || ' minutes')::INTERVAL) < :currentTime
        """, nativeQuery = true)
    List<Waitlist> findWaitlistsToRefund(@Param("currentTime") LocalDateTime currentTime);
}
