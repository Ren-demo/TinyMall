package com.mall.tinymall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j(Swagger)配置类（适配Jakarta）
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置OpenAPI文档基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API文档标题
                .info(new Info()
                        .title("API测试文档")
                        // API文档描述
                        .description("tinyshop测试文档")
                        // API版本
                        .version("1.0.0"));
    }
}