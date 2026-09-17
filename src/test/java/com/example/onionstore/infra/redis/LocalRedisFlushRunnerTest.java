package com.example.onionstore.infra.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalRedisFlushRunnerTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;
    @Mock
    private RedisConnection redisConnection;
    @Mock
    private RedisServerCommands redisServerCommands;

    @InjectMocks
    private LocalRedisFlushRunner runner;

    @Test
    void 로컬_시작시_현재_레디스_DB를_초기화한다() throws Exception {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.serverCommands()).thenReturn(redisServerCommands);

        runner.run(new DefaultApplicationArguments());

        verify(redisServerCommands).flushDb();
        verify(redisConnection).close();
    }
}
