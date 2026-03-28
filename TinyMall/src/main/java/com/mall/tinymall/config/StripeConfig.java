package com.mall.tinymall.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    // 项目启动时初始化 Stripe 客户端
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeApiKey;
    }
}