package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.ClassDefinitionRepository;
import com.alvin.bookingsystem.domain.repository.ClassScheduleRepository;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.dto.request.ClassScheduleFilter;
import com.alvin.bookingsystem.dto.request.ClassScheduleRequest;
import com.alvin.bookingsystem.dto.response.ClassScheduleResponse;
import com.alvin.bookingsystem.mapper.ClassScheduleMapper;
import com.alvin.bookingsystem.service.ClassScheduleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassScheduleServiceImpl extends BaseServiceImpl<ClassSchedule, ClassScheduleRequest, ClassScheduleResponse, ClassScheduleFilter> implements ClassScheduleService {

    private final ClassScheduleMapper classScheduleMapper;
    private final ClassDefinitionRepository classDefinitionRepository;
    private final CountryRepository countryRepository;

    public ClassScheduleServiceImpl(ClassScheduleRepository classScheduleRepository,
                                    ClassScheduleMapper classScheduleMapper,
                                    ClassDefinitionRepository classDefinitionRepository,
                                    CountryRepository countryRepository) {
        super(classScheduleRepository);
        this.classScheduleMapper = classScheduleMapper;
        this.classDefinitionRepository = classDefinitionRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    protected ClassSchedule mapRequestToEntity(ClassScheduleRequest request) {
        ClassDefinition classDefinition = classDefinitionRepository.findById(request.classDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException("ClassDefinition not found with id: " + request.classDefinitionId()));
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        return classScheduleMapper.toEntity(request, classDefinition, country);
    }

    @Override
    protected ClassScheduleResponse mapEntityToResponse(ClassSchedule entity) {
        return classScheduleMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(ClassSchedule entity, ClassScheduleRequest request) {
        ClassDefinition classDefinition = null;
        if (request.classDefinitionId() != null) {
            classDefinition = classDefinitionRepository.findById(request.classDefinitionId())
                    .orElseThrow(() -> new EntityNotFoundException("ClassDefinition not found with id: " + request.classDefinitionId()));
        }
        Country country = null;
        if (request.countryId() != null) {
            country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        }
        classScheduleMapper.updateEntity(entity, request, classDefinition, country);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.CLASS_SCHEDULES);
    }
}
