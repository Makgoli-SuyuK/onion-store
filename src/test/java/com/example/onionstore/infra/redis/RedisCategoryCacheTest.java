package com.example.onionstore.infra.redis;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.support.RedisSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisSupport.class)
class RedisCategoryCacheTest {
    @Autowired
    private RedisCategoryCache redisCategoryCache;


    @Test
    @DisplayName("redis 저장 및 조회 테스트")
    void getAndPut() {
        //given
        Category category = new Category("category");
        ReflectionTestUtils.setField(category, "id", 1L);

        redisCategoryCache.put(List.of(category));

        //when
        List<Category> result = redisCategoryCache.get();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo(category.getName());
    }

    @Test
    void evict() {
        //given
        Category category = new Category("category");
        ReflectionTestUtils.setField(category, "id", 1L);

        redisCategoryCache.put(List.of(category));

        //when
        redisCategoryCache.evict();

        //then
        assertThat(redisCategoryCache.get()).isNull();
    }
}