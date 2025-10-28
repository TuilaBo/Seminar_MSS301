package com.khoavdse170395.orchestratorservice.service;

public interface AssignmentServiceClient {
    boolean checkEligibility(Long assignmentId, String userId);
    void reserveAttempt(Long assignmentId, String userId, String idempotencyKey);
    void releaseAttempt(Long assignmentId, String userId, String idempotencyKey);
}