package com.example.onionstore.infra.cache;

import com.example.onionstore.domain.product.repository.cache.ProductStockCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaffeineProductStockCache implements ProductStockCache {
    private final Cache<Long, Integer> cache;

    @Override
    public Integer get(Long productId) {
        return cache.getIfPresent(productId);
    }

    @Override
    public void put(Long productId, Integer stock) {
        cache.put(productId, stock);
    }

    @Override
    public void evict(Long productId) {
        cache.invalidate(productId);
    }
}
