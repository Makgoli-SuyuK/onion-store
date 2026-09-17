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
                .get(RedisProperty.productLike(productId));

        if (value == null) {
            return null;
        }

        return Long.parseLong(value);
    }

    @Override
    public void put(Long productId, Long likeCount) {
        redisTemplate.opsForValue()
                .set(RedisProperty.productLike(productId), String.valueOf(likeCount), RedisProperty.PRODUCT_LIKE_DURATION);
    }

    @Override
    public void evict(Long productId) {
        redisTemplate.delete(RedisProperty.productLike(productId));
    }
}
