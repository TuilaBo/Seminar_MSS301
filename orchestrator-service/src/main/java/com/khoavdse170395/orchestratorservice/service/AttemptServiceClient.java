package com.khoavdse170395.orchestratorservice.service;

import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;

public interface AttemptServiceClient {
    Long createAttempt(Long assignmentId, String userId, String idempotencyKey);
    void cancelAttempt(Long attemptId);
    void submitAnswers(Long attemptId, SubmitAttemptRequest request);
    Double autoScore(Long attemptId);
    void finalizeAttempt(Long attemptId, Double score);
}