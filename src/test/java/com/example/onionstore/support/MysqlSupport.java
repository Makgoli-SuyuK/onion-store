package com.example.onionstore.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MysqlSupport {
    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {
        return new MySQLContainer("mysql:latest")
                .withDatabaseName("onionstore")
                .withUsername("test")
                .withPassword("test");
    }
}
