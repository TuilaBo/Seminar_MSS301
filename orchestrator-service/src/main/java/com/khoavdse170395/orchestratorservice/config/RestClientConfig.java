package com.khoavdse170395.orchestratorservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${orchestrator.http.connectTimeoutMs}")
    private int connectTimeoutMs;

    @Value("${orchestrator.http.readTimeoutMs}")
    private int readTimeoutMs;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .defaultStatusHandler(
                        httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        (request, response) -> {
                            throw new RuntimeException("HTTP error: " + response.getStatusCode());
                        }
                );
    }
}





