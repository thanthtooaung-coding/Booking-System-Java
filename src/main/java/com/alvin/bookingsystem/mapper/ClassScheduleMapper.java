package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.dto.request.ClassScheduleRequest;
import com.alvin.bookingsystem.dto.response.ClassScheduleResponse;
import org.springframework.stereotype.Component;

@Component
public class ClassScheduleMapper {

    public ClassSchedule toEntity(ClassScheduleRequest request, ClassDefinition classDefinition, Country country) {
        ClassSchedule.Status status = request.status() != null
                ? ClassSchedule.Status.valueOf(request.status())
                : ClassSchedule.Status.SCHEDULED;

        return ClassSchedule.builder()
                .classDefinition(classDefinition)
                .country(country)
                .classDateTime(request.classDateTime())
                .maxSlots(request.maxSlots())
                .bookedSlots(0)
                .status(status)
                .build();
    }

    public ClassScheduleResponse toResponse(ClassSchedule entity) {
        if (entity == null) {
            return null;
        }

        String className = null;
        if (entity.getClassDefinition() != null) {
            className = entity.getClassDefinition().getName();
        }

        String countryName = null;
        if (entity.getCountry() != null) {
            countryName = entity.getCountry().getName();
        }

        return ClassScheduleResponse.builder()
                .id(entity.getId())
                .classDefinitionId(entity.getClassDefinition() != null ? entity.getClassDefinition().getId() : null)
                .className(className)
                .classDescription(entity.getClassDefinition() != null ? entity.getClassDefinition().getDescription() : null)
                .requiredCredits(entity.getClassDefinition() != null ? entity.getClassDefinition().getRequiredCredits() : null)
                .durationMinutes(entity.getClassDefinition() != null ? entity.getClassDefinition().getDurationMinutes() : null)
                .instructorName(entity.getClassDefinition() != null ? entity.getClassDefinition().getInstructorName() : null)
                .countryId(entity.getCountry() != null ? entity.getCountry().getId() : null)
                .countryName(countryName)
                .countryCode(entity.getCountry() != null ? entity.getCountry().getCode() : null)
                .classDateTime(entity.getClassDateTime())
                .maxSlots(entity.getMaxSlots())
                .bookedSlots(entity.getBookedSlots())
                .availableSlots(entity.getMaxSlots() != null && entity.getBookedSlots() != null
                        ? entity.getMaxSlots() - entity.getBookedSlots()
                        : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .build();
    }

    public void updateEntity(ClassSchedule entity, ClassScheduleRequest request, ClassDefinition classDefinition, Country country) {
        if (classDefinition != null) {
            entity.setClassDefinition(classDefinition);
        }
        if (country != null) {
            entity.setCountry(country);
        }
        if (request.classDateTime() != null) {
            entity.setClassDateTime(request.classDateTime());
        }
        if (request.maxSlots() != null) {
            entity.setMaxSlots(request.maxSlots());
        }
        if (request.status() != null) {
            entity.setStatus(ClassSchedule.Status.valueOf(request.status()));
        }
    }
}
