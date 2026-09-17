package com.example.onionstore.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * local 프로필에서만 Redis의 현재 DB를 비운다.
 * data.sql이 DB를 다시 만들 때 이전 실행의 캐시가 남지 않게 한다.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalRedisFlushRunner implements ApplicationRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(ApplicationArguments args) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }

        log.info("로컬 시작 시 Redis 캐시를 초기화했습니다.");
    }
}
