package com.alvin.bookingsystem.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * DRY helper around {@link CacheManager} for entity response caching keyed by id.
 * Disabled when {@code app.cache.crud-enabled=false} (no Redis lookups).
 */
@Component
public class CrudResponseCache {

    private final CacheManager cacheManager;

    @Value("${app.cache.crud-enabled:true}")
    private boolean enabled;

    public CrudResponseCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<Object> get(String region, Long id) {
        if (!enabled) {
            return Optional.empty();
        }
        Cache cache = cacheManager.getCache(region);
        if (cache == null) {
            return Optional.empty();
        }
        Cache.ValueWrapper wrapper = cache.get(id);
        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }
        return Optional.of(wrapper.get());
    }

    public void put(String region, Long id, Object response) {
        if (!enabled) {
            return;
        }
        Cache cache = cacheManager.getCache(region);
        if (cache != null) {
            cache.put(id, response);
        }
    }

    public void evict(String region, Long id) {
        if (!enabled) {
            return;
        }
        Cache cache = cacheManager.getCache(region);
        if (cache != null) {
            cache.evict(id);
        }
    }

    /** Clears all entries for a region (e.g. future list caches). */
    public void clearRegion(@Nullable String region) {
        if (!enabled || region == null) {
            return;
        }
        Cache cache = cacheManager.getCache(region);
        if (cache != null) {
            cache.clear();
        }
    }
}
