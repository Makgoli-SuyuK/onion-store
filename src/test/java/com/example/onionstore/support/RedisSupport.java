package com.example.onionstore.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class RedisSupport {
    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> redis =
                new GenericContainer<>("redis:latest")
                        .withExposedPorts(6379);

        redis.start();
        return redis;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(GenericContainer<?> redis) {
        return new LettuceConnectionFactory(
                redis.getHost(),
                redis.getMappedPort(6379)
        );
    }
}
