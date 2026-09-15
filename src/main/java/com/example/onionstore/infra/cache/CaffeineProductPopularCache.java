package com.example.onionstore.infra.cache;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.repository.cache.ProductPopularCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CaffeineProductPopularCache implements ProductPopularCache {
    private final Cache<String, List<ProductSimpleResponse>> cache;
    private static final String KEY = "top10";

    @Override
    public List<ProductSimpleResponse> get() {
        return cache.getIfPresent(KEY);
    }

    @Override
    public void put(List<ProductSimpleResponse> list) {
        cache.put(KEY, list);
    }

    @Override
    public void evict() {
        cache.invalidate(KEY);
    }
}
