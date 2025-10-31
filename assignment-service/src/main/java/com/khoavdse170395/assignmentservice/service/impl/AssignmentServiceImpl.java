package com.khoavdse170395.assignmentservice.service.impl;

import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityResponse;
import com.khoavdse170395.assignmentservice.model.dto.CreateAssignmentRequest;
import com.khoavdse170395.assignmentservice.model.dto.CreateAssignmentResponse;
import com.khoavdse170395.assignmentservice.model.dto.RemainingAttemptsResponse;
import com.khoavdse170395.assignmentservice.model.AssignmentAttemptCounter;
import com.khoavdse170395.assignmentservice.model.QuizAssignment;
import com.khoavdse170395.assignmentservice.config.DemoFlags;
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
    private final DemoFlags demoFlags;

    // In-memory map to store idempotency keys for reserve operations
    // In a real-world scenario, this would be a distributed cache like Redis
    private final Map<String, Boolean> idempotencyKeyStore = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public CreateAssignmentResponse createAssignment(CreateAssignmentRequest request) {
        log.info("Creating assignment for quizId: {}, maxAttempts: {}", request.getQuizId(), request.getMaxAttempts());

        QuizAssignment assignment = QuizAssignment.builder()
                .quizId(request.getQuizId())
                .allowedGroup(request.getAllowedGroup())
                .openAt(request.getOpenAt())
                .closeAt(request.getCloseAt())
                .maxAttempts(request.getMaxAttempts())
                .build();

        quizAssignmentRepository.save(assignment);

        return CreateAssignmentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .maxAttempts(assignment.getMaxAttempts())
                .build();
    }

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

        if (Boolean.TRUE.equals(demoFlags.isFailReserve())) {
            log.warn("Demo flag failReserve is ON. Throwing to simulate failure.");
            throw new IllegalStateException("Demo: reserve failed by flag");
        }

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

        // Only release if the exact reserve with this key had succeeded before
        if (!idempotencyKeyStore.containsKey(idempotencyKey)) {
            log.warn("Skip release: no successful reserve found for idempotencyKey {}", idempotencyKey);
            return;
        }

        Optional<AssignmentAttemptCounter> counterOpt = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        if (counterOpt.isPresent() && counterOpt.get().getUsed() > 0) {
            attemptCounterRepository.decrementUsedAttempts(assignmentId, userId);
            // Mark the key as released to avoid double-release; also forget the reserve marker
            idempotencyKeyStore.remove(idempotencyKey);
            log.info("Successfully released attempt for assignmentId: {}, userId: {}", assignmentId, userId);
        } else {
            log.warn("No attempts to release for assignmentId: {}, userId: {}", assignmentId, userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RemainingAttemptsResponse getRemainingAttempts(Long assignmentId, String userId) {
        Optional<QuizAssignment> assignmentOpt = quizAssignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            return RemainingAttemptsResponse.builder().remainingAttempts(0).build();
        }
        QuizAssignment assignment = assignmentOpt.get();
        Instant now = Instant.now();
        if ((assignment.getOpenAt() != null && now.isBefore(assignment.getOpenAt())) ||
                (assignment.getCloseAt() != null && now.isAfter(assignment.getCloseAt()))) {
            return RemainingAttemptsResponse.builder().remainingAttempts(0).build();
        }

        Optional<AssignmentAttemptCounter> counterOpt = attemptCounterRepository.findByAssignmentIdAndUserId(assignmentId, userId);
        int used = counterOpt.map(AssignmentAttemptCounter::getUsed).orElse(0);
        int remaining = Math.max(0, assignment.getMaxAttempts() - used);
        return RemainingAttemptsResponse.builder().remainingAttempts(remaining).build();
    }
}
