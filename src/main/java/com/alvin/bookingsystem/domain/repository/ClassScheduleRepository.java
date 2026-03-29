package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.ClassSchedule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassScheduleRepository extends BaseRepository<ClassSchedule> {
    @Modifying
    @Query(value = "UPDATE class_schedules SET created_by_id = :createdById WHERE id = :classScheduleId", nativeQuery = true)
    void updateCreatedById(@Param("classScheduleId") Long classScheduleId, @Param("createdById") Long createdById);
}
