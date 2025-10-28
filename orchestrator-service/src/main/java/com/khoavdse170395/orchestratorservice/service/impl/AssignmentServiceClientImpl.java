package com.khoavdse170395.orchestratorservice.service.impl;

import com.khoavdse170395.orchestratorservice.service.AssignmentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class AssignmentServiceClientImpl implements AssignmentServiceClient {

    @Value("${orchestrator.endpoints.assignment}")
    private String assignmentServiceUrl;

    @Value("${orchestrator.http.connectTimeoutMs}")
    private int connectTimeoutMs;

    @Value("${orchestrator.http.readTimeoutMs}")
    private int readTimeoutMs;

    private final RestClient restClient;

    public AssignmentServiceClientImpl(RestClient.Builder restClientBuilder,
                                     @Value("${orchestrator.endpoints.assignment}") String assignmentServiceUrl,
                                     @Value("${orchestrator.http.connectTimeoutMs}") int connectTimeoutMs,
                                     @Value("${orchestrator.http.readTimeoutMs}") int readTimeoutMs) {
        this.assignmentServiceUrl = assignmentServiceUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.restClient = restClientBuilder
                .baseUrl(assignmentServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public boolean checkEligibility(Long assignmentId, String userId) {
        log.info("Checking eligibility for assignmentId: {}, userId: {}", assignmentId, userId);
        
        try {
            Map<String, Object> request = Map.of("userId", userId);
            
            ResponseEntity<Map> response = restClient.post()
                    .uri("/assignments/{assignmentId}/check-eligibility", assignmentId)
                    .body(request)
                    .retrieve()
                    .toEntity(Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Boolean eligible = (Boolean) response.getBody().get("eligible");
                log.info("Eligibility check result: {}", eligible);
                return Boolean.TRUE.equals(eligible);
            }
            
            log.warn("Eligibility check failed with status: {}", response.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("Error checking eligibility for assignmentId: {}, userId: {}", assignmentId, userId, e);
            throw new RuntimeException("Failed to check eligibility", e);
        }
    }

    @Override
    public void reserveAttempt(Long assignmentId, String userId, String idempotencyKey) {
        log.info("Reserving attempt for assignmentId: {}, userId: {}, idempotencyKey: {}", 
                assignmentId, userId, idempotencyKey);
        
        try {
            Map<String, Object> request = Map.of(
                    "userId", userId,
                    "idempotencyKey", idempotencyKey
            );
            
            restClient.post()
                    .uri("/assignments/{assignmentId}/reserve", assignmentId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully reserved attempt for assignmentId: {}", assignmentId);
        } catch (Exception e) {
            log.error("Error reserving attempt for assignmentId: {}, userId: {}", assignmentId, userId, e);
            throw new RuntimeException("Failed to reserve attempt", e);
        }
    }

    @Override
    public void releaseAttempt(Long assignmentId, String userId, String idempotencyKey) {
        log.info("Releasing attempt for assignmentId: {}, userId: {}, idempotencyKey: {}", 
                assignmentId, userId, idempotencyKey);
        
        try {
            Map<String, Object> request = Map.of(
                    "userId", userId,
                    "idempotencyKey", idempotencyKey
            );
            
            restClient.post()
                    .uri("/assignments/{assignmentId}/release", assignmentId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully released attempt for assignmentId: {}", assignmentId);
        } catch (Exception e) {
            log.error("Error releasing attempt for assignmentId: {}, userId: {}", assignmentId, userId, e);
            // Don't throw exception for compensation - best effort
        }
    }
}

