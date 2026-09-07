package com.example.onionstore.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI onionMallOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ONION MALL API")
                        .version("v1")
                        .description("ONION MALL 백엔드 API 문서입니다."));
    }
}
