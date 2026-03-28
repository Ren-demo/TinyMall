package com.mall.tinymall.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${spring.minio.endpoint}")
    private String endpoint;

    @Value("${spring.minio.access-key}")
    private String accessKey;

    @Value("${spring.minio.secret-key}")
    private String secretKey;

    @Value("${spring.minio.secure}")
    private boolean secure;


    /**
     * 构建MinioClient实例，交给Spring容器管理
     */
    @Bean
    public MinioClient minioClient() {
        log.info("初始化minio");
        return MinioClient.builder()
                .credentials(accessKey, secretKey)
                .endpoint(endpoint, 9000, secure)
                .build();
    }
}