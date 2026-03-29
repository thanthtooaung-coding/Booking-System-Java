package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.cache.CrudResponseCache;
import com.alvin.bookingsystem.domain.model.MasterEntity;
import com.alvin.bookingsystem.domain.repository.BaseRepository;
import com.alvin.bookingsystem.domain.repository.specification.GenericSpecification;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.BaseService;
import com.alvin.bookingsystem.util.PaginationHelper;
import com.google.gson.Gson;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.util.ClassUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseServiceImpl<ENTITY, REQUEST, RESPONSE, FILTER> implements BaseService<REQUEST, RESPONSE, FILTER> {

    protected final BaseRepository<ENTITY> repository;
    protected final JpaSpecificationExecutor<ENTITY> specificationExecutor;
    private final GenericSpecification<ENTITY> genericSpecification = new GenericSpecification<>();

    @Autowired(required = false)
    private CrudResponseCache crudResponseCache;

    @Autowired
    private Gson gson;

    private volatile Class<RESPONSE> responseTypeCache;

    protected BaseServiceImpl(BaseRepository<ENTITY> repository) {
        this.repository = repository;
        this.specificationExecutor = repository;
    }

    /**
     * Override with {@code Optional.of(CacheRegions.X)} to enable Redis-backed response caching for this service.
     */
    protected Optional<String> cacheRegion() {
        return Optional.empty();
    }

    protected boolean isCrudCacheActive() {
        return crudResponseCache != null && crudResponseCache.isEnabled() && cacheRegion().isPresent();
    }

    protected Long entityId(ENTITY entity) {
        if (entity instanceof MasterEntity me) {
            return me.getId();
        }
        throw new IllegalStateException("Entity must extend MasterEntity for CRUD cache");
    }

    /** Call when the entity is updated outside the standard CRUD update path (e.g. auth or batch flows). */
    protected void evictCrudCacheForId(Long id) {
        if (isCrudCacheActive()) {
            crudResponseCache.evict(cacheRegion().get(), id);
        }
    }

    /** Evict another cache region (e.g. {@code user-packages} when waitlist credits change). */
    protected void evictCrudCacheRegion(String region, Long id) {
        if (crudResponseCache != null && crudResponseCache.isEnabled()) {
            crudResponseCache.evict(region, id);
        }
    }

    /**
     * Called after mapping the request to an entity and before {@code save} on create.
     * Use for side effects such as reserving credits on the related {@code UserPackage}.
     */
    protected void beforePersistOnCreate(ENTITY entity) {
    }

    /**
     * Called after loading the entity and before {@code deleteById} on delete.
     */
    protected void beforeDelete(ENTITY entity) {
    }

    @Override
    @Transactional
    public RESPONSE create(REQUEST request) {
        validateBeforeCreate(request);
        ENTITY entity = mapRequestToEntity(request);
        beforePersistOnCreate(entity);
        ENTITY savedEntity = repository.save(entity);
        RESPONSE response = mapEntityToResponse(savedEntity);
        if (isCrudCacheActive()) {
            crudResponseCache.put(cacheRegion().get(), entityId(savedEntity), response);
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RESPONSE findById(Long id) {
        if (isCrudCacheActive()) {
            Optional<Object> cached = crudResponseCache.get(cacheRegion().get(), id);
            if (cached.isPresent()) {
                return materializeCachedResponse(cached.get());
            }
        }
        ENTITY entity = findByIdOrThrow(id);
        RESPONSE response = mapEntityToResponse(entity);
        if (isCrudCacheActive()) {
            crudResponseCache.put(cacheRegion().get(), id, response);
        }
        return response;
    }

    /**
     * Redis JSON deserializes to {@link java.util.LinkedHashMap}; convert back to the concrete RESPONSE DTO.
     */
    private RESPONSE materializeCachedResponse(Object value) {
        return gson.fromJson(gson.toJson(value), responseType());
    }

    @SuppressWarnings("unchecked")
    private Class<RESPONSE> responseType() {
        Class<RESPONSE> local = responseTypeCache;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (responseTypeCache == null) {
                Class<?> r = ResolvableType.forClass(ClassUtils.getUserClass(this))
                        .as(BaseServiceImpl.class)
                        .getGeneric(2)
                        .resolve();
                if (r == null) {
                    throw new IllegalStateException("Cannot resolve RESPONSE type for " + getClass().getName());
                }
                responseTypeCache = (Class<RESPONSE>) r;
            }
            return responseTypeCache;
        }
    }

    @Override
    @Transactional
    public RESPONSE update(Long id, REQUEST request) {
        ENTITY entity = findByIdOrThrow(id);
        validateBeforeUpdate(id, request, entity);
        updateEntityFromRequest(entity, request);
        ENTITY updatedEntity = repository.save(entity);
        RESPONSE response = mapEntityToResponse(updatedEntity);
        if (isCrudCacheActive()) {
            crudResponseCache.put(cacheRegion().get(), id, response);
        }
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ENTITY entity = findByIdOrThrow(id);
        beforeDelete(entity);
        repository.deleteById(id);
        if (isCrudCacheActive()) {
            crudResponseCache.evict(cacheRegion().get(), id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationDTO<RESPONSE> getAll(PageAndFilterDTO<FILTER> pageAndFilterDTO) {
        if (pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()) {
            validateSortBy(pageAndFilterDTO.getSortBy());
        }
        
        FILTER filter = pageAndFilterDTO.getFilter();
        Map<String, Object> keywordMap = toMap(filter, getFieldMapping());
        
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        Specification<ENTITY> spec = genericSpecification.getSpecification(keywordMap, fields);
        
        Pageable pageable = buildPageable(pageAndFilterDTO);
        
        Page<ENTITY> page;
        if (spec != null) {
            page = specificationExecutor.findAll(spec, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        
        List<RESPONSE> content = page.getContent().stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return PaginationHelper.getResponse(page, content);
    }

    private Pageable buildPageable(PageAndFilterDTO<FILTER> pageAndFilterDTO) {
        String sortBy = pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()
                ? pageAndFilterDTO.getSortBy()
                : "id";
        String sortDirection = pageAndFilterDTO.getSortDirection() != null && !pageAndFilterDTO.getSortDirection().isEmpty()
                ? pageAndFilterDTO.getSortDirection()
                : "ASC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(pageAndFilterDTO.getPage(), pageAndFilterDTO.getSize(), sort);
    }

    protected abstract ENTITY mapRequestToEntity(REQUEST request);

    protected abstract RESPONSE mapEntityToResponse(ENTITY entity);

    protected abstract void updateEntityFromRequest(ENTITY entity, REQUEST request);

    /**
     * Override this method to validate before creating a new entity.
     * Use this to check for duplicate unique fields (e.g., email, code, name).
     * Throw DuplicateEntityException if validation fails.
     * 
     * @param request The request DTO containing data for the new entity
     * @throws DuplicateEntityException if a unique constraint would be violated
     */
    protected void validateBeforeCreate(REQUEST request) {
        // Default implementation does nothing
        // Override in subclasses to add validation logic
    }

    /**
     * Override this method to validate before updating an existing entity.
     * Use this to check for duplicate unique fields, excluding the current entity.
     * Throw DuplicateEntityException if validation fails.
     * 
     * @param id The ID of the entity being updated
     * @param request The request DTO containing updated data
     * @param existingEntity The existing entity being updated
     * @throws DuplicateEntityException if a unique constraint would be violated
     */
    protected void validateBeforeUpdate(Long id, REQUEST request, ENTITY existingEntity) {
        // Default implementation does nothing
        // Override in subclasses to add validation logic
    }

    /**
     * Override this method to provide field mapping for nested properties.
     * Example: Map.of("profileId", "profileId.profileId", "skillSubcategoryId", "skillSubcategoryId.skillSubcategoryId")
     */
    protected Map<String, String> getFieldMapping() {
        return Map.of();
    }

    /**
     * Override this method to provide custom validation for sortBy field.
     * Default implementation does nothing - JPA will handle invalid sort fields.
     * Override in subclasses to validate against response DTO fields if needed.
     */
    protected void validateSortBy(String sortBy) {
        // Default implementation - can be overridden in subclasses for custom validation
        // JPA will throw an exception if the sort field is invalid
    }

    protected ENTITY findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    private Map<String, Object> toMap(FILTER filter, Map<String, String> fieldMapping) {
        if (filter == null) {
            return Map.of();
        }

        Map<String, Object> map = new java.util.HashMap<>();
        Field[] fields = filter.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(filter);
                if (value != null) {
                    String fieldName = field.getName();
                    String mappedField = fieldMapping.getOrDefault(fieldName, fieldName);
                    map.put(mappedField, value);
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return map;
    }
}
