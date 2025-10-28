package com.khoavdse170395.assignmentservice;

import com.khoavdse170395.assignmentservice.model.AssignmentAttemptCounter;
import com.khoavdse170395.assignmentservice.model.QuizAssignment;
import com.khoavdse170395.assignmentservice.repository.AssignmentAttemptCounterRepository;
import com.khoavdse170395.assignmentservice.repository.QuizAssignmentRepository;
import com.khoavdse170395.assignmentservice.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AssignmentServiceIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private QuizAssignmentRepository quizAssignmentRepository;

    @Autowired
    private AssignmentAttemptCounterRepository attemptCounterRepository;

    @Test
    public void testAssignmentEligibilityFlow() {
        // Create a test assignment
        QuizAssignment assignment = QuizAssignment.builder()
                .quizId(1L)
                .allowedGroup("test-group")
                .openAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .closeAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .maxAttempts(2)
                .build();
        
        QuizAssignment savedAssignment = quizAssignmentRepository.save(assignment);
        Long assignmentId = savedAssignment.getAssignmentId();
        String userId = "test-user-123";

        // Test eligibility check - should be eligible
        var eligibilityResponse = assignmentService.checkEligibility(assignmentId, userId);
        assertTrue(eligibilityResponse.isEligible());
        assertEquals("Eligible to attempt", eligibilityResponse.getReason());

        // Reserve first attempt
        String idempotencyKey1 = "key-1";
        assignmentService.reserveAttempt(assignmentId, userId, idempotencyKey1);

        // Check counter was created
        var counter = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        assertTrue(counter.isPresent());
        assertEquals(1, counter.get().getUsed());

        // Reserve second attempt
        String idempotencyKey2 = "key-2";
        assignmentService.reserveAttempt(assignmentId, userId, idempotencyKey2);

        // Check counter was incremented
        counter = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        assertTrue(counter.isPresent());
        assertEquals(2, counter.get().getUsed());

        // Try to reserve third attempt - should fail
        String idempotencyKey3 = "key-3";
        assertThrows(IllegalStateException.class, () -> {
            assignmentService.reserveAttempt(assignmentId, userId, idempotencyKey3);
        });

        // Release one attempt
        assignmentService.releaseAttempt(assignmentId, userId, "release-key-1");

        // Check counter was decremented
        counter = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        assertTrue(counter.isPresent());
        assertEquals(1, counter.get().getUsed());

        // Now should be eligible again
        eligibilityResponse = assignmentService.checkEligibility(assignmentId, userId);
        assertTrue(eligibilityResponse.isEligible());
    }
}

