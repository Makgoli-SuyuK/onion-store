package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.repository.cache.ProductPopularCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisProductPopularCache implements ProductPopularCache {
    private final RedisTemplate<String, List<ProductSimpleResponse>> redisTemplate;

    @Override
    public List<ProductSimpleResponse> get() {
        return redisTemplate.opsForValue()
                .get(RedisKey.PRODUCT_POPULAR);
    }

    @Override
    public void put(List<ProductSimpleResponse> list) {
        redisTemplate.opsForValue()
                .set(RedisKey.PRODUCT_POPULAR, list);
    }

    @Override
    public void evict() {
        redisTemplate.delete(RedisKey.PRODUCT_POPULAR);
    }
}
