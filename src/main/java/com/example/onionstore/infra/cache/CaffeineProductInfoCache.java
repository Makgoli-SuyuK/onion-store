package com.example.onionstore.infra.cache;

import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.domain.product.repository.cache.ProductInfoCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaffeineProductInfoCache implements ProductInfoCache {
    private final Cache<Long, ProductDto> cache;

    @Override
    public ProductDto get(Long productId) {
        return cache.getIfPresent(productId);
    }

    @Override
    public void put(Long productId, ProductDto productDto) {
        cache.put(productId, productDto);
    }

    @Override
    public void evict(Long id) {
        cache.invalidate(id);
    }
}
