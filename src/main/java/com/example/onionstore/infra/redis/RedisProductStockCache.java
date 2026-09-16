package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.repository.cache.ProductStockCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisProductStockCache implements ProductStockCache {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Integer get(Long productId) {
        String value = stringRedisTemplate.opsForValue()
                .get(RedisKey.productStock(productId));

        if (value == null) {
            return null;
        }

        return Integer.valueOf(value);
    }

    @Override
    public void put(Long productId, Integer stock) {
        stringRedisTemplate.opsForValue()
                .set(RedisKey.productStock(productId), String.valueOf(stock));
    }

    @Override
    public void evict(Long productId) {
        stringRedisTemplate.delete(RedisKey.productStock(productId));
    }
}
