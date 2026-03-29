package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.dto.request.CountryFilter;
import com.alvin.bookingsystem.dto.request.CountryRequest;
import com.alvin.bookingsystem.dto.response.CountryResponse;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.mapper.CountryMapper;
import com.alvin.bookingsystem.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CountryServiceImpl extends BaseServiceImpl<Country, CountryRequest, CountryResponse, CountryFilter> implements CountryService {

    private final CountryMapper countryMapper;
    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository, CountryMapper countryMapper) {
        super(countryRepository);
        this.countryMapper = countryMapper;
        this.countryRepository = countryRepository;
    }

    @Override
    protected void validateBeforeCreate(CountryRequest request) {
        if (request.code() != null && countryRepository.findByCode(request.code()).isPresent()) {
            throw new DuplicateEntityException("code", request.code());
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, CountryRequest request, Country existingEntity) {
        if (request.code() != null && !request.code().equals(existingEntity.getCode())) {
            if (countryRepository.findByCode(request.code()).isPresent()) {
                throw new DuplicateEntityException("code", request.code());
            }
        }
    }

    @Override
    protected Country mapRequestToEntity(CountryRequest request) {
        return countryMapper.toEntity(request);
    }

    @Override
    protected CountryResponse mapEntityToResponse(Country entity) {
        return countryMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Country entity, CountryRequest request) {
        countryMapper.updateEntity(entity, request);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.COUNTRIES);
    }
}
