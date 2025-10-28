package com.khoavdse170395.assignmentservice.service.impl;

import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityResponse;
import com.khoavdse170395.assignmentservice.model.AssignmentAttemptCounter;
import com.khoavdse170395.assignmentservice.model.QuizAssignment;
import com.khoavdse170395.assignmentservice.repository.AssignmentAttemptCounterRepository;
import com.khoavdse170395.assignmentservice.repository.QuizAssignmentRepository;
import com.khoavdse170395.assignmentservice.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final QuizAssignmentRepository quizAssignmentRepository;
    private final AssignmentAttemptCounterRepository attemptCounterRepository;

    // In-memory map to store idempotency keys for reserve operations
    // In a real-world scenario, this would be a distributed cache like Redis
    private final Map<String, Boolean> idempotencyKeyStore = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public CheckEligibilityResponse checkEligibility(Long assignmentId, String userId) {
        log.info("Checking eligibility for assignmentId: {}, userId: {}", assignmentId, userId);
        Optional<QuizAssignment> assignmentOpt = quizAssignmentRepository.findById(assignmentId);

        if (assignmentOpt.isEmpty()) {
            return CheckEligibilityResponse.builder().eligible(false).reason("Assignment not found").build();
        }

        QuizAssignment assignment = assignmentOpt.get();
        Instant now = Instant.now();

        if (assignment.getOpenAt() != null && now.isBefore(assignment.getOpenAt())) {
            return CheckEligibilityResponse.builder().eligible(false).reason("Assignment not yet open").build();
        }

        if (assignment.getCloseAt() != null && now.isAfter(assignment.getCloseAt())) {
            return CheckEligibilityResponse.builder().eligible(false).reason("Assignment has closed").build();
        }

        Optional<AssignmentAttemptCounter> counterOpt = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        int usedAttempts = counterOpt.map(AssignmentAttemptCounter::getUsed).orElse(0);

        if (usedAttempts >= assignment.getMaxAttempts()) {
            return CheckEligibilityResponse.builder().eligible(false).reason("Maximum attempts reached").build();
        }

        return CheckEligibilityResponse.builder().eligible(true).reason("Eligible").build();
    }

    @Override
    @Transactional
    public void reserveAttempt(Long assignmentId, String userId, String idempotencyKey) {
        log.info("Reserving attempt for assignmentId: {}, userId: {}, idempotencyKey: {}", assignmentId, userId, idempotencyKey);

        // Idempotency check
        if (idempotencyKeyStore.containsKey(idempotencyKey)) {
            log.info("Idempotency key {} already processed. Skipping reservation.", idempotencyKey);
            return;
        }

        CheckEligibilityResponse eligibility = checkEligibility(assignmentId, userId);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("Cannot reserve attempt: " + eligibility.getReason());
        }

        Optional<AssignmentAttemptCounter> counterOpt = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        if (counterOpt.isPresent()) {
            attemptCounterRepository.incrementUsedAttempts(assignmentId, userId);
        } else {
            AssignmentAttemptCounter newCounter = AssignmentAttemptCounter.builder()
                    .assignmentId(assignmentId)
                    .userId(userId)
                    .used(1)
                    .build();
            attemptCounterRepository.save(newCounter);
        }
        idempotencyKeyStore.put(idempotencyKey, true); // Mark key as processed
        log.info("Successfully reserved attempt for assignmentId: {}, userId: {}", assignmentId, userId);
    }

    @Override
    @Transactional
    public void releaseAttempt(Long assignmentId, String userId, String idempotencyKey) {
        log.info("Releasing attempt for assignmentId: {}, userId: {}, idempotencyKey: {}", assignmentId, userId, idempotencyKey);

        // Idempotency check (optional for release, but good practice)
        if (idempotencyKeyStore.containsKey("release-" + idempotencyKey)) {
            log.info("Release idempotency key {} already processed. Skipping release.", idempotencyKey);
            return;
        }

        Optional<AssignmentAttemptCounter> counterOpt = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        if (counterOpt.isPresent() && counterOpt.get().getUsed() > 0) {
            attemptCounterRepository.decrementUsedAttempts(assignmentId, userId);
            idempotencyKeyStore.put("release-" + idempotencyKey, true); // Mark release key as processed
            log.info("Successfully released attempt for assignmentId: {}, userId: {}", assignmentId, userId);
        } else {
            log.warn("No attempts to release for assignmentId: {}, userId: {}", assignmentId, userId);
        }
    }
}
