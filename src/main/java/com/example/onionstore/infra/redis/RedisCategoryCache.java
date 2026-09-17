package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.cache.CategoryCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisCategoryCache implements CategoryCache {
    private final RedisTemplate<String, List<Category>> redisTemplate;

    @Override
    public List<Category> get() {
        return redisTemplate.opsForValue().get(RedisProperty.CATEGORY);
    }

    @Override
    public void put(List<Category> list) {
        redisTemplate.opsForValue().set(RedisProperty.CATEGORY, list, RedisProperty.CATEGORY_DURATION);
    }

    @Override
    public void evict() {
        redisTemplate.delete(RedisProperty.CATEGORY);
    }
}
