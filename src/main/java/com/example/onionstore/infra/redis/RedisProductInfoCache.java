package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.domain.product.repository.cache.ProductInfoCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisProductInfoCache implements ProductInfoCache {
    private final RedisTemplate<String, ProductDto> productDtoRedisTemplate;

    @Override
    public ProductDto get(Long productId) {
        return productDtoRedisTemplate.opsForValue()
                .get(RedisKey.productInfo(productId));
    }

    @Override
    public void put(Long productId, ProductDto productDto) {
        productDtoRedisTemplate.opsForValue().set(RedisKey.productInfo(productId), productDto);

    }

    @Override
    public void evict(Long productId) {
        productDtoRedisTemplate.delete(RedisKey.productInfo(productId));
    }
}
