package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.domain.repository.CreditPackageRepository;
import com.alvin.bookingsystem.dto.request.CreditPackageFilter;
import com.alvin.bookingsystem.dto.request.CreditPackageRequest;
import com.alvin.bookingsystem.dto.response.CreditPackageResponse;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.mapper.CreditPackageMapper;
import com.alvin.bookingsystem.service.CreditPackageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreditPackageServiceImpl extends BaseServiceImpl<CreditPackage, CreditPackageRequest, CreditPackageResponse, CreditPackageFilter> implements CreditPackageService {

    private final CreditPackageMapper creditPackageMapper;
    private final CreditPackageRepository creditPackageRepository;
    private final CountryRepository countryRepository;

    public CreditPackageServiceImpl(CreditPackageRepository creditPackageRepository,
                                    CreditPackageMapper creditPackageMapper,
                                    CountryRepository countryRepository) {
        super(creditPackageRepository);
        this.creditPackageMapper = creditPackageMapper;
        this.creditPackageRepository = creditPackageRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    protected void validateBeforeCreate(CreditPackageRequest request) {
        if (request.name() != null && request.countryId() != null) {
            if (creditPackageRepository.findByNameAndCountryId(request.name(), request.countryId()).isPresent()) {
                throw new DuplicateEntityException(
                    String.format("Package name '%s' already exists for this country", request.name())
                );
            }
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, CreditPackageRequest request, CreditPackage existingEntity) {
        if (request.name() != null && request.countryId() != null) {
            Long countryId = request.countryId() != null ? request.countryId() : existingEntity.getCountry().getId();
            String name = request.name();

            creditPackageRepository.findByNameAndCountryId(name, countryId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateEntityException(
                            String.format("Package name '%s' already exists for this country", name)
                        );
                    }
                });
        }
    }

    @Override
    protected CreditPackage mapRequestToEntity(CreditPackageRequest request) {
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        return creditPackageMapper.toEntity(request, country);
    }

    @Override
    protected CreditPackageResponse mapEntityToResponse(CreditPackage entity) {
        return creditPackageMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(CreditPackage entity, CreditPackageRequest request) {
        Country country = null;
        if (request.countryId() != null) {
            country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + request.countryId()));
        }
        creditPackageMapper.updateEntity(entity, request, country);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.CREDIT_PACKAGES);
    }
}
