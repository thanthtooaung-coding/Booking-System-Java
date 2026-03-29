package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.ClassDefinitionRepository;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.dto.request.ClassDefinitionFilter;
import com.alvin.bookingsystem.dto.request.ClassDefinitionRequest;
import com.alvin.bookingsystem.dto.response.ClassDefinitionResponse;
import com.alvin.bookingsystem.mapper.ClassDefinitionMapper;
import com.alvin.bookingsystem.service.ClassDefinitionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassDefinitionServiceImpl extends BaseServiceImpl<ClassDefinition, ClassDefinitionRequest, ClassDefinitionResponse, ClassDefinitionFilter> implements ClassDefinitionService {

    private final ClassDefinitionMapper classDefinitionMapper;
    private final CountryRepository countryRepository;

    public ClassDefinitionServiceImpl(ClassDefinitionRepository classDefinitionRepository,
                                      ClassDefinitionMapper classDefinitionMapper,
                                      CountryRepository countryRepository) {
        super(classDefinitionRepository);
        this.classDefinitionMapper = classDefinitionMapper;
        this.countryRepository = countryRepository;
    }

    @Override
    protected ClassDefinition mapRequestToEntity(ClassDefinitionRequest request) {
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        return classDefinitionMapper.toEntity(request, country);
    }

    @Override
    protected ClassDefinitionResponse mapEntityToResponse(ClassDefinition entity) {
        return classDefinitionMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(ClassDefinition entity, ClassDefinitionRequest request) {
        Country country = null;
        if (request.countryId() != null) {
            country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        }
        classDefinitionMapper.updateEntity(entity, request, country);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.CLASS_DEFINITIONS);
    }
}
