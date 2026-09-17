package com.example.onionstore.global.config;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        GenericJacksonJsonRedisSerializer serializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .build();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, ProductDto> redisProductDtoTemplate(
            RedisConnectionFactory factory
    ) {
        RedisTemplate<String, ProductDto> template = new RedisTemplate<>();

        JacksonJsonRedisSerializer<ProductDto> serializer =
                new JacksonJsonRedisSerializer<>(ProductDto.class);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setConnectionFactory(factory);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, List<ProductSimpleResponse>> productPopularRedisTemplate(
            RedisConnectionFactory factory
    ) {
        JsonMapper mapper = new JsonMapper();

        RedisTemplate<String, List<ProductSimpleResponse>> template = new RedisTemplate<>();

        JavaType type = mapper.getTypeFactory()
                .constructCollectionType(
                        List.class,
                        ProductSimpleResponse.class
                );

        JacksonJsonRedisSerializer<List<ProductSimpleResponse>> serializer =
                new JacksonJsonRedisSerializer<>(mapper, type);

        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();

        template.setConnectionFactory(factory);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, List<Category>> categoryRedisTemplate(
            RedisConnectionFactory factory
    ) {
        JsonMapper mapper = new JsonMapper();

        RedisTemplate<String, List<Category>> template = new RedisTemplate<>();

        JavaType type = mapper.getTypeFactory()
                .constructCollectionType(
                        List.class,
                        Category.class
                );

        JacksonJsonRedisSerializer<List<Category>> serializer =
                new JacksonJsonRedisSerializer<>(mapper, type);

        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();

        template.setConnectionFactory(factory);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }
}
