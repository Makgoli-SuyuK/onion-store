package com.example.onionstore.infra.cache;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.cache.CategoryCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CaffeineCategoryCache implements CategoryCache {
    private final Cache<String, List<Category>> cache;
    private static final String KEY = "category";

    @Override
    public List<Category> get() {
        return cache.getIfPresent(KEY);
    }

    @Override
    public void put(List<Category> list) {
        cache.put(KEY, list);
    }

    @Override
    public void evict() {
        cache.invalidate(KEY);
    }
}
