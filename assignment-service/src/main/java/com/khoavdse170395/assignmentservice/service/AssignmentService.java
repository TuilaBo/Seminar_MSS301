package com.khoavdse170395.assignmentservice.service;

import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityResponse;
import com.khoavdse170395.assignmentservice.model.dto.CreateAssignmentRequest;
import com.khoavdse170395.assignmentservice.model.dto.CreateAssignmentResponse;
import com.khoavdse170395.assignmentservice.model.dto.RemainingAttemptsResponse;

public interface AssignmentService {
    CreateAssignmentResponse createAssignment(CreateAssignmentRequest request);
    CheckEligibilityResponse checkEligibility(Long assignmentId, String userId);
    void reserveAttempt(Long assignmentId, String userId, String idempotencyKey);
    void releaseAttempt(Long assignmentId, String userId, String idempotencyKey);
    RemainingAttemptsResponse getRemainingAttempts(Long assignmentId, String userId);
}


