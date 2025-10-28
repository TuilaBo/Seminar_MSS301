package com.khoavdse170395.assignmentservice.service;

import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityResponse;

public interface AssignmentService {
    CheckEligibilityResponse checkEligibility(Long assignmentId, String userId);
    void reserveAttempt(Long assignmentId, String userId, String idempotencyKey);
    void releaseAttempt(Long assignmentId, String userId, String idempotencyKey);
}

