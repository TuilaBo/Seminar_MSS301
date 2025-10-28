package com.khoavdse170395.attemptservice;

import com.khoavdse170395.attemptservice.model.QuizAttempt;
import com.khoavdse170395.attemptservice.model.UserAnswer;
import com.khoavdse170395.attemptservice.model.dto.*;
import com.khoavdse170395.attemptservice.repository.QuizAttemptRepository;
import com.khoavdse170395.attemptservice.repository.UserAnswerRepository;
import com.khoavdse170395.attemptservice.service.AttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AttemptServiceIntegrationTest {

    @Autowired
    private AttemptService attemptService;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Test
    public void testCompleteAttemptFlow() {
        // Test data
        Long assignmentId = 1L;
        String userId = "test-user-123";
        String idempotencyKey = "test-key-123";

        // 1. Create attempt
        CreateAttemptRequest createRequest = CreateAttemptRequest.builder()
                .assignmentId(assignmentId)
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .build();

        CreateAttemptResponse createResponse = attemptService.createAttempt(createRequest);
        assertNotNull(createResponse.getAttemptId());
        assertEquals("PENDING", createResponse.getStatus());

        Long attemptId = createResponse.getAttemptId();

        // Verify attempt was created
        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findById(attemptId);
        assertTrue(attemptOpt.isPresent());
        QuizAttempt attempt = attemptOpt.get();
        assertEquals(assignmentId, attempt.getAssignmentId());
        assertEquals(userId, attempt.getUserId());
        assertEquals("PENDING", attempt.getStatus());
        assertEquals(idempotencyKey, attempt.getIdempotencyKey());

        // 2. Test idempotency - create same attempt again
        CreateAttemptResponse duplicateResponse = attemptService.createAttempt(createRequest);
        assertEquals(attemptId, duplicateResponse.getAttemptId());
        assertEquals("PENDING", duplicateResponse.getStatus());

        // 3. Submit answers
        SubmitAnswersRequest submitRequest = SubmitAnswersRequest.builder()
                .answers(Arrays.asList(
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(1L)
                                .selectedOption("correct")
                                .build(),
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(2L)
                                .selectedOption("wrong")
                                .build(),
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(3L)
                                .selectedOption("correct")
                                .build()
                ))
                .build();

        attemptService.submitAnswers(attemptId, submitRequest);

        // Verify answers were saved
        List<UserAnswer> answers = userAnswerRepository.findByAttemptId(attemptId);
        assertEquals(3, answers.size());

        // 4. Auto-score
        AutoScoreResponse scoreResponse = attemptService.autoScore(attemptId);
        assertNotNull(scoreResponse.getScore());
        assertEquals(new BigDecimal("6.70"), scoreResponse.getScore()); // 2/3 * 10 = 6.70

        // Verify answers were marked as correct/incorrect
        answers = userAnswerRepository.findByAttemptId(attemptId);
        assertEquals(3, answers.size());
        
        UserAnswer answer1 = answers.stream().filter(a -> a.getQuestionId().equals(1L)).findFirst().orElseThrow();
        assertTrue(answer1.getIsCorrect());
        
        UserAnswer answer2 = answers.stream().filter(a -> a.getQuestionId().equals(2L)).findFirst().orElseThrow();
        assertFalse(answer2.getIsCorrect());
        
        UserAnswer answer3 = answers.stream().filter(a -> a.getQuestionId().equals(3L)).findFirst().orElseThrow();
        assertTrue(answer3.getIsCorrect());

        // 5. Finalize attempt
        FinalizeAttemptRequest finalizeRequest = FinalizeAttemptRequest.builder()
                .score(new BigDecimal("6.70"))
                .build();

        attemptService.finalizeAttempt(attemptId, finalizeRequest);

        // Verify attempt was finalized
        attemptOpt = quizAttemptRepository.findById(attemptId);
        assertTrue(attemptOpt.isPresent());
        attempt = attemptOpt.get();
        assertEquals("SUBMITTED", attempt.getStatus());
        assertEquals(new BigDecimal("6.70"), attempt.getScore());
        assertNotNull(attempt.getFinishedAt());
    }

    @Test
    public void testCancelAttempt() {
        // Create attempt
        CreateAttemptRequest createRequest = CreateAttemptRequest.builder()
                .assignmentId(1L)
                .userId("test-user-456")
                .idempotencyKey("cancel-test-key")
                .build();

        CreateAttemptResponse createResponse = attemptService.createAttempt(createRequest);
        Long attemptId = createResponse.getAttemptId();

        // Cancel attempt
        attemptService.cancelAttempt(attemptId);

        // Verify attempt was canceled
        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findById(attemptId);
        assertTrue(attemptOpt.isPresent());
        QuizAttempt attempt = attemptOpt.get();
        assertEquals("CANCELED", attempt.getStatus());
        assertNotNull(attempt.getFinishedAt());
    }

    @Test
    public void testUpdateAnswers() {
        // Create attempt and submit initial answers
        CreateAttemptRequest createRequest = CreateAttemptRequest.builder()
                .assignmentId(1L)
                .userId("test-user-789")
                .idempotencyKey("update-test-key")
                .build();

        CreateAttemptResponse createResponse = attemptService.createAttempt(createRequest);
        Long attemptId = createResponse.getAttemptId();

        // Submit initial answers
        SubmitAnswersRequest initialRequest = SubmitAnswersRequest.builder()
                .answers(Arrays.asList(
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(1L)
                                .selectedOption("A")
                                .build()
                ))
                .build();

        attemptService.submitAnswers(attemptId, initialRequest);

        // Update answers
        SubmitAnswersRequest updateRequest = SubmitAnswersRequest.builder()
                .answers(Arrays.asList(
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(1L)
                                .selectedOption("B")
                                .build(),
                        SubmitAnswersRequest.AnswerDto.builder()
                                .questionId(2L)
                                .selectedOption("C")
                                .build()
                ))
                .build();

        attemptService.submitAnswers(attemptId, updateRequest);

        // Verify answers were updated/added
        List<UserAnswer> answers = userAnswerRepository.findByAttemptId(attemptId);
        assertEquals(2, answers.size());

        UserAnswer answer1 = userAnswerRepository.findByAttemptIdAndQuestionId(attemptId, 1L).orElseThrow();
        assertEquals("B", answer1.getSelectedOption());

        UserAnswer answer2 = userAnswerRepository.findByAttemptIdAndQuestionId(attemptId, 2L).orElseThrow();
        assertEquals("C", answer2.getSelectedOption());
    }
}
