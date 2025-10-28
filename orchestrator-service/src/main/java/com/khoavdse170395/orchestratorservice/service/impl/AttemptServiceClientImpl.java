package com.khoavdse170395.orchestratorservice.service.impl;

import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;
import com.khoavdse170395.orchestratorservice.service.AttemptServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class AttemptServiceClientImpl implements AttemptServiceClient {

    @Value("${orchestrator.endpoints.attempt}")
    private String attemptServiceUrl;

    private final RestClient restClient;

    public AttemptServiceClientImpl(RestClient.Builder restClientBuilder,
                                  @Value("${orchestrator.endpoints.attempt}") String attemptServiceUrl) {
        this.attemptServiceUrl = attemptServiceUrl;
        this.restClient = restClientBuilder
                .baseUrl(attemptServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public Long createAttempt(Long assignmentId, String userId, String idempotencyKey) {
        log.info("Creating attempt for assignmentId: {}, userId: {}, idempotencyKey: {}", 
                assignmentId, userId, idempotencyKey);
        
        try {
            Map<String, Object> request = Map.of(
                    "assignmentId", assignmentId,
                    "userId", userId,
                    "idempotencyKey", idempotencyKey
            );
            
            ResponseEntity<Map> response = restClient.post()
                    .uri("/attempts")
                    .body(request)
                    .retrieve()
                    .toEntity(Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Long attemptId = ((Number) response.getBody().get("attemptId")).longValue();
                log.info("Successfully created attempt with ID: {}", attemptId);
                return attemptId;
            }
            
            throw new RuntimeException("Failed to create attempt, status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error creating attempt for assignmentId: {}, userId: {}", assignmentId, userId, e);
            throw new RuntimeException("Failed to create attempt", e);
        }
    }

    @Override
    public void cancelAttempt(Long attemptId) {
        log.info("Canceling attempt: {}", attemptId);
        
        try {
            restClient.post()
                    .uri("/attempts/{attemptId}/cancel", attemptId)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully canceled attempt: {}", attemptId);
        } catch (Exception e) {
            log.error("Error canceling attempt: {}", attemptId, e);
            // Don't throw exception for compensation - best effort
        }
    }

    @Override
    public void submitAnswers(Long attemptId, SubmitAttemptRequest request) {
        log.info("Submitting answers for attempt: {}, answers count: {}", 
                attemptId, request.getAnswers().size());
        
        try {
            Map<String, Object> requestBody = Map.of(
                    "answers", request.getAnswers()
            );
            
            restClient.post()
                    .uri("/attempts/{attemptId}/answers", attemptId)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully submitted answers for attempt: {}", attemptId);
        } catch (Exception e) {
            log.error("Error submitting answers for attempt: {}", attemptId, e);
            throw new RuntimeException("Failed to submit answers", e);
        }
    }

    @Override
    public Double autoScore(Long attemptId) {
        log.info("Auto-scoring attempt: {}", attemptId);
        
        try {
            ResponseEntity<Map> response = restClient.post()
                    .uri("/attempts/{attemptId}/autoscore", attemptId)
                    .retrieve()
                    .toEntity(Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Double score = ((Number) response.getBody().get("score")).doubleValue();
                log.info("Auto-scored attempt {} with score: {}", attemptId, score);
                return score;
            }
            
            throw new RuntimeException("Failed to auto-score attempt, status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error auto-scoring attempt: {}", attemptId, e);
            throw new RuntimeException("Failed to auto-score attempt", e);
        }
    }

    @Override
    public void finalizeAttempt(Long attemptId, Double score) {
        log.info("Finalizing attempt: {} with score: {}", attemptId, score);
        
        try {
            Map<String, Object> request = Map.of("score", score);
            
            restClient.post()
                    .uri("/attempts/{attemptId}/finalize", attemptId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully finalized attempt: {} with score: {}", attemptId, score);
        } catch (Exception e) {
            log.error("Error finalizing attempt: {} with score: {}", attemptId, score, e);
            throw new RuntimeException("Failed to finalize attempt", e);
        }
    }
}

