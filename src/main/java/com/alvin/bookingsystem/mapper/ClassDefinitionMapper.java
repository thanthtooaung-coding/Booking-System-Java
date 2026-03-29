package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.dto.request.ClassDefinitionRequest;
import com.alvin.bookingsystem.dto.response.ClassDefinitionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassDefinitionMapper {

    private final MasterDataMapper masterDataMapper;

    public ClassDefinition toEntity(ClassDefinitionRequest request, Country country) {
        return ClassDefinition.builder()
                .name(request.name())
                .country(country)
                .requiredCredits(request.requiredCredits())
                .durationMinutes(request.durationMinutes())
                .description(request.description())
                .instructorName(request.instructorName())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    public ClassDefinitionResponse toResponse(ClassDefinition entity) {
        if (entity == null) {
            return null;
        }

        String countryName = null;
        if (entity.getCountry() != null) {
            countryName = entity.getCountry().getName();
        }

        return ClassDefinitionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .countryId(entity.getCountry() != null ? entity.getCountry().getId() : null)
                .countryName(countryName)
                .requiredCredits(entity.getRequiredCredits())
                .durationMinutes(entity.getDurationMinutes())
                .description(entity.getDescription())
                .instructorName(entity.getInstructorName())
                .active(entity.getActive())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(ClassDefinition entity, ClassDefinitionRequest request, Country country) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (country != null) {
            entity.setCountry(country);
        }
        if (request.requiredCredits() != null) {
            entity.setRequiredCredits(request.requiredCredits());
        }
        if (request.durationMinutes() != null) {
            entity.setDurationMinutes(request.durationMinutes());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.instructorName() != null) {
            entity.setInstructorName(request.instructorName());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
