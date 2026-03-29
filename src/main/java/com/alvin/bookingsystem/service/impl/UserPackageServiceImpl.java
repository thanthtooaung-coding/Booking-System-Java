package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.repository.CreditPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.dto.request.UserPackageFilter;
import com.alvin.bookingsystem.dto.request.UserPackageRequest;
import com.alvin.bookingsystem.dto.response.UserPackageResponse;
import com.alvin.bookingsystem.mapper.UserPackageMapper;
import com.alvin.bookingsystem.service.UserPackageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserPackageServiceImpl extends BaseServiceImpl<UserPackage, UserPackageRequest, UserPackageResponse, UserPackageFilter> implements UserPackageService {

    private final UserPackageMapper userPackageMapper;
    private final UserRepository userRepository;
    private final CreditPackageRepository creditPackageRepository;

    public UserPackageServiceImpl(UserPackageRepository userPackageRepository,
                                  UserPackageMapper userPackageMapper,
                                  UserRepository userRepository,
                                  CreditPackageRepository creditPackageRepository) {
        super(userPackageRepository);
        this.userPackageMapper = userPackageMapper;
        this.userRepository = userRepository;
        this.creditPackageRepository = creditPackageRepository;
    }

    @Override
    protected UserPackage mapRequestToEntity(UserPackageRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));
        CreditPackage creditPackage = creditPackageRepository.findById(request.creditPackageId())
                .orElseThrow(() -> new EntityNotFoundException("CreditPackage not found with id: " + request.creditPackageId()));
        return userPackageMapper.toEntity(request, user, creditPackage);
    }

    @Override
    protected UserPackageResponse mapEntityToResponse(UserPackage entity) {
        return userPackageMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(UserPackage entity, UserPackageRequest request) {
        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));
        }
        CreditPackage creditPackage = null;
        if (request.creditPackageId() != null) {
            creditPackage = creditPackageRepository.findById(request.creditPackageId())
                    .orElseThrow(() -> new EntityNotFoundException("CreditPackage not found with id: " + request.creditPackageId()));
        }
        userPackageMapper.updateEntity(entity, request, user, creditPackage);
    }

    @Override
    protected Optional<String> cacheRegion() {
        return Optional.of(CacheRegions.USER_PACKAGES);
    }
}
