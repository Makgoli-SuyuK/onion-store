package com.example.onionstore.infra.cache;

import com.example.onionstore.domain.product.repository.cache.ProductLikeCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaffeineProductLikeCache implements ProductLikeCache {
    private final Cache<Long, Long> cache;

    @Override
    public Long get(Long productId) {
        return cache.getIfPresent(productId);
    }

    @Override
    public void put(Long productId, Long likeCount) {
        cache.put(productId, likeCount);
    }

    @Override
    public void evict(Long productId) {
        cache.invalidate(productId);
    }
}
