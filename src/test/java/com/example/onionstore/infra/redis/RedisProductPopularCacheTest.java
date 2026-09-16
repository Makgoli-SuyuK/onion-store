package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.repository.cache.ProductPopularCache;
import com.example.onionstore.support.RedisSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisSupport.class)
class RedisProductPopularCacheTest {
    @Autowired
    private ProductPopularCache productPopularCache;

    @Test
    @DisplayName("redis의 데이터를 저장 및 조회한다.")
    void getAndSet() {
        //given
        List<ProductSimpleResponse> list = List.of(
                new ProductSimpleResponse(
                        1L,
                        "category",
                        "name",
                        1000L,
                        10
                )
        );

        productPopularCache.put(list);

        //when
        List<ProductSimpleResponse> result = productPopularCache.get();

        //then
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).categoryName()).isEqualTo("category");
        assertThat(result.get(0).name()).isEqualTo("name");
    }

    @Test
    @DisplayName("redis의 데이터를 삭제한다")
    void evict() {
        //given
        List<ProductSimpleResponse> list = List.of(
                new ProductSimpleResponse(
                        1L,
                        "category",
                        "name",
                        1000L,
                        10
                )
        );

        productPopularCache.put(list);

        //when
        productPopularCache.evict();

        //then
        List<ProductSimpleResponse> result = productPopularCache.get();

        assertThat(result).isNull();
    }
}