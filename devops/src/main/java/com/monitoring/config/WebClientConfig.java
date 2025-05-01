package com.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Web client yapılandırması.
 */
@Configuration
public class WebClientConfig {

    /**
     * RestTemplate bean'i oluşturur.
     *
     * @return RestTemplate nesnesi
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * WebClient bean'i oluşturur.
     *
     * @return WebClient.Builder nesnesi
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}