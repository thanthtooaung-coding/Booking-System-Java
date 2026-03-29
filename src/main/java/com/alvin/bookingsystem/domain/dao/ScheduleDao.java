package com.alvin.bookingsystem.domain.dao;

import com.alvin.bookingsystem.dto.response.ClassScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get available class schedules by country
     */
    public List<ClassScheduleResponse> findAvailableSchedulesByCountryId(Long countryId) {
        String sql = """
            SELECT 
                cs.id,
                cs.class_definition_id,
                cd.name as class_name,
                cd.description as class_description,
                cd.required_credits,
                cd.duration_minutes,
                cd.instructor_name,
                cs.country_id,
                c.name as country_name,
                c.code as country_code,
                cs.class_datetime,
                cs.max_slots,
                cs.booked_slots,
                (cs.max_slots - cs.booked_slots) as available_slots,
                cs.status
            FROM class_schedules cs
            JOIN class_definitions cd ON cs.class_definition_id = cd.id
            JOIN countries c ON cs.country_id = c.id
            WHERE cs.country_id = ?
              AND cs.class_datetime > NOW()
              AND cs.status = 'SCHEDULED'
              AND cd.active = TRUE
            ORDER BY cs.class_datetime ASC
            """;
        return jdbcTemplate.query(sql, new ClassScheduleRowMapper(), countryId);
    }

    private static class ClassScheduleRowMapper implements RowMapper<ClassScheduleResponse> {
        @Override
        public ClassScheduleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClassScheduleResponse.builder()
                    .id(rs.getLong("id"))
                    .classDefinitionId(rs.getLong("class_definition_id"))
                    .className(rs.getString("class_name"))
                    .classDescription(rs.getString("class_description"))
                    .requiredCredits(rs.getInt("required_credits"))
                    .durationMinutes(rs.getInt("duration_minutes"))
                    .instructorName(rs.getString("instructor_name"))
                    .countryId(rs.getLong("country_id"))
                    .countryName(rs.getString("country_name"))
                    .countryCode(rs.getString("country_code"))
                    .classDateTime(rs.getObject("class_datetime", LocalDateTime.class))
                    .maxSlots(rs.getInt("max_slots"))
                    .bookedSlots(rs.getInt("booked_slots"))
                    .availableSlots(rs.getInt("available_slots"))
                    .status(rs.getString("status"))
                    .build();
        }
    }
}
