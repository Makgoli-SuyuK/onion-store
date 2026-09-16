package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.support.RedisSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import({RedisSupport.class})
class RedisProductInfoCacheTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisProductInfoCache redisProductInfoCache;

    @BeforeEach
    void tearDown() {
        ProductDto dto = new ProductDto(
                1L,
                "category",
                "name",
                "description",
                1000L,
                "SELLING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        redisTemplate.opsForValue().set("product:1", dto);
    }

    @AfterEach
    void tearDownAfter() {
        redisTemplate.delete("product:2");
    }

    @Test
    @DisplayName("저장되어 있는 캐시를 Redis에서 가져온다")
    void redis에서_저장된_정보_추출() {
        //given
        Long productId = 1L;

        //when
        ProductDto dto = redisProductInfoCache.get(productId);

        //then
        assertThat(dto.id()).isEqualTo(productId);
        assertThat(dto.category()).isEqualTo("category");
        assertThat(dto.description()).isEqualTo("description");
        assertThat(dto.createdAt()).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("정보를 redis 캐시에 저장한다")
    void put() {
        //given
        Long productId = 2L;

        ProductDto dto = new ProductDto(
                2L,
                "category",
                "name2",
                "description",
                20000L,
                "SELLING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        redisProductInfoCache.put(productId, dto);

        //then
        ProductDto result = redisProductInfoCache.get(productId);

        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo(dto.name());
        assertThat(result.category()).isEqualTo("category");
        assertThat(result.description()).isEqualTo("description");
    }

    @Test
    @DisplayName("redis 캐시에서 정보를 삭제한다")
    void evict() {
        //given
        Long productId = 2L;

        ProductDto dto = new ProductDto(
                2L,
                "category",
                "name2",
                "description",
                20000L,
                "SELLING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        redisProductInfoCache.put(productId, dto);

        //when
        redisProductInfoCache.evict(productId);

        //then
        ProductDto result = redisProductInfoCache.get(productId);

        assertThat(result).isNull();
    }
}