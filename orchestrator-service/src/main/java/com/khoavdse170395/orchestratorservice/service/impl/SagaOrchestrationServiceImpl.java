package com.khoavdse170395.orchestratorservice.service.impl;

import com.khoavdse170395.orchestratorservice.dto.StartAttemptRequest;
import com.khoavdse170395.orchestratorservice.dto.StartAttemptResponse;
import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;
import com.khoavdse170395.orchestratorservice.service.AssignmentServiceClient;
import com.khoavdse170395.orchestratorservice.service.AttemptServiceClient;
import com.khoavdse170395.orchestratorservice.service.SagaOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SagaOrchestrationServiceImpl implements SagaOrchestrationService {

    private final AssignmentServiceClient assignmentServiceClient;
    private final AttemptServiceClient attemptServiceClient;
    
    // Hard-coded demo user as per requirements
    private static final String DEMO_USER_ID = "demo-user";

    public SagaOrchestrationServiceImpl(AssignmentServiceClient assignmentServiceClient,
                                      AttemptServiceClient attemptServiceClient) {
        this.assignmentServiceClient = assignmentServiceClient;
        this.attemptServiceClient = attemptServiceClient;
    }

    @Override
    public StartAttemptResponse startAttempt(StartAttemptRequest request) {
        log.info("Starting SAGA for assignmentId: {}", request.getAssignmentId());
        
        Long assignmentId = request.getAssignmentId();
        String idempotencyKey = UUID.randomUUID().toString();
        Long attemptId = null;
        
        try {
            // Step 1: Check eligibility
            log.info("Step 1: Checking eligibility for assignmentId: {}", assignmentId);
            boolean eligible = assignmentServiceClient.checkEligibility(assignmentId, DEMO_USER_ID);
            if (!eligible) {
                throw new RuntimeException("User not eligible for assignment: " + assignmentId);
            }
            log.info("Step 1 completed: User is eligible");
            
            // Step 2: Create attempt
            log.info("Step 2: Creating attempt for assignmentId: {}", assignmentId);
            attemptId = attemptServiceClient.createAttempt(assignmentId, DEMO_USER_ID, idempotencyKey);
            log.info("Step 2 completed: Created attempt with ID: {}", attemptId);
            
            // Step 3: Reserve attempt
            log.info("Step 3: Reserving attempt for assignmentId: {}", assignmentId);
            assignmentServiceClient.reserveAttempt(assignmentId, DEMO_USER_ID, idempotencyKey);
            log.info("Step 3 completed: Reserved attempt");
            
            log.info("SAGA completed successfully for assignmentId: {}, attemptId: {}", assignmentId, attemptId);
            return StartAttemptResponse.builder()
                    .attemptId(attemptId)
                    .status("PENDING")
                    .build();
                    
        } catch (Exception e) {
            log.error("SAGA failed for assignmentId: {}, attemptId: {}", assignmentId, attemptId, e);
            
            // Compensation logic
            performCompensation(assignmentId, attemptId, idempotencyKey);
            
            throw new RuntimeException("SAGA failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void submitAttempt(SubmitAttemptRequest request) {
        log.info("Submitting attempt for attemptId: {}", request.getAttemptId());
        
        Long attemptId = request.getAttemptId();
        
        try {
            // Step 1: Submit answers
            log.info("Step 1: Submitting answers for attemptId: {}", attemptId);
            attemptServiceClient.submitAnswers(attemptId, request);
            log.info("Step 1 completed: Submitted answers");
            
            // Step 2: Auto-score
            log.info("Step 2: Auto-scoring attemptId: {}", attemptId);
            Double score = attemptServiceClient.autoScore(attemptId);
            log.info("Step 2 completed: Auto-scored with score: {}", score);
            
            // Step 3: Finalize attempt
            log.info("Step 3: Finalizing attemptId: {} with score: {}", attemptId, score);
            attemptServiceClient.finalizeAttempt(attemptId, score);
            log.info("Step 3 completed: Finalized attempt");
            
            log.info("Submit attempt SAGA completed successfully for attemptId: {}", attemptId);
            
        } catch (Exception e) {
            log.error("Submit attempt SAGA failed for attemptId: {}", attemptId, e);
            throw new RuntimeException("Submit attempt SAGA failed: " + e.getMessage(), e);
        }
    }

    private void performCompensation(Long assignmentId, Long attemptId, String idempotencyKey) {
        log.info("Performing compensation for assignmentId: {}, attemptId: {}, idempotencyKey: {}", 
                assignmentId, attemptId, idempotencyKey);
        
        // Compensation Step 1: Cancel attempt (if created)
        if (attemptId != null) {
            try {
                log.info("Compensation Step 1: Canceling attempt: {}", attemptId);
                attemptServiceClient.cancelAttempt(attemptId);
                log.info("Compensation Step 1 completed: Canceled attempt");
            } catch (Exception e) {
                log.error("Compensation Step 1 failed: Failed to cancel attempt: {}", attemptId, e);
            }
        }
        
        // Compensation Step 2: Release assignment reservation (best effort)
        try {
            log.info("Compensation Step 2: Releasing assignment reservation for assignmentId: {}", assignmentId);
            assignmentServiceClient.releaseAttempt(assignmentId, DEMO_USER_ID, idempotencyKey);
            log.info("Compensation Step 2 completed: Released assignment reservation");
        } catch (Exception e) {
            log.error("Compensation Step 2 failed: Failed to release assignment reservation for assignmentId: {}", 
                    assignmentId, e);
        }
        
        log.info("Compensation completed for assignmentId: {}, attemptId: {}", assignmentId, attemptId);
    }
}

