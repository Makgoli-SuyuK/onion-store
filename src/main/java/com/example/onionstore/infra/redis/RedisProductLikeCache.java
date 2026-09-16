package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.repository.cache.ProductLikeCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisProductLikeCache implements ProductLikeCache {
    private final StringRedisTemplate redisTemplate;

    @Override
    public Long get(Long productId) {
        String value = redisTemplate.opsForValue()
                .get(RedisKey.productInfo(productId));

        if (value == null) {
            return null;
        }

        return Long.parseLong(value);
    }

    @Override
    public void put(Long productId, Long likeCount) {
        redisTemplate.opsForValue()
                .set(RedisKey.productInfo(productId), String.valueOf(likeCount));
    }

    @Override
    public void evict(Long productId) {
        redisTemplate.delete(RedisKey.productInfo(productId));
    }
}
