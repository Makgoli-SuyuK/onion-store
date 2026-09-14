package com.example.onionstore.global.config;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean
    public Cache<String, List<Category>> categoryListCache() {
        return Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(Duration.ofMinutes(180))
                .recordStats()
                .build();
    }
}
