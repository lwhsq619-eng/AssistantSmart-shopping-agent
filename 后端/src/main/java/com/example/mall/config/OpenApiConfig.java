package com.example.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI mallOpenApi() {
        return new OpenAPI().info(new Info()
                .title("网上商城购物系统接口文档")
                .description("Spring Boot 3 + MyBatis-Plus RESTful API")
                .version("1.0.0"));
    }
}
