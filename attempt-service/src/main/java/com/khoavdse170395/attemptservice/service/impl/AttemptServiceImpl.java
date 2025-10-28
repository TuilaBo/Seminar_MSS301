package com.khoavdse170395.attemptservice.service.impl;

import com.khoavdse170395.attemptservice.model.dto.AutoScoreResponse;
import com.khoavdse170395.attemptservice.model.dto.CreateAttemptRequest;
import com.khoavdse170395.attemptservice.model.dto.CreateAttemptResponse;
import com.khoavdse170395.attemptservice.model.dto.FinalizeAttemptRequest;
import com.khoavdse170395.attemptservice.model.dto.SubmitAnswersRequest;
import com.khoavdse170395.attemptservice.model.QuizAttempt;
import com.khoavdse170395.attemptservice.model.UserAnswer;
import com.khoavdse170395.attemptservice.repository.QuizAttemptRepository;
import com.khoavdse170395.attemptservice.repository.UserAnswerRepository;
import com.khoavdse170395.attemptservice.service.AttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptServiceImpl implements AttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Override
    @Transactional
    public CreateAttemptResponse createAttempt(CreateAttemptRequest request) {
        log.info("Creating attempt for assignmentId: {}, userId: {}, idempotencyKey: {}",
                request.getAssignmentId(), request.getUserId(), request.getIdempotencyKey());

        // Idempotency check
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingAttempt.isPresent()) {
            log.info("Attempt already exists with idempotency key: {}", request.getIdempotencyKey());
            return CreateAttemptResponse.builder()
                    .attemptId(existingAttempt.get().getAttemptId())
                    .status(existingAttempt.get().getStatus())
                    .build();
        }

        QuizAttempt newAttempt = QuizAttempt.builder()
                .assignmentId(request.getAssignmentId())
                .userId(request.getUserId())
                .status("PENDING")
                .startedAt(Instant.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        quizAttemptRepository.save(newAttempt);
        log.info("Successfully created attempt with ID: {}", newAttempt.getAttemptId());

        return CreateAttemptResponse.builder()
                .attemptId(newAttempt.getAttemptId())
                .status(newAttempt.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void cancelAttempt(Long attemptId) {
        log.info("Canceling attempt: {}", attemptId);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!"PENDING".equals(attempt.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING attempts can be canceled");
        }

        attempt.setStatus("CANCELED");
        attempt.setFinishedAt(Instant.now());
        quizAttemptRepository.save(attempt);
        log.info("Successfully canceled attempt: {}", attemptId);
    }

    @Override
    @Transactional
    public void submitAnswers(Long attemptId, SubmitAnswersRequest request) {
        log.info("Submitting answers for attempt: {}", attemptId);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!"PENDING".equals(attempt.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answers can only be submitted for PENDING attempts");
        }

        for (SubmitAnswersRequest.AnswerDto answerDto : request.getAnswers()) {
            Optional<UserAnswer> existingAnswer = userAnswerRepository.findByAttemptIdAndQuestionId(attemptId, answerDto.getQuestionId());

            UserAnswer answer;
            if (existingAnswer.isPresent()) {
                answer = existingAnswer.get();
                answer.setSelectedOption(answerDto.getSelectedOption());
                answer.setAnswerText(answerDto.getAnswerText());
            } else {
                answer = UserAnswer.builder()
                        .attemptId(attemptId)
                        .questionId(answerDto.getQuestionId())
                        .selectedOption(answerDto.getSelectedOption())
                        .answerText(answerDto.getAnswerText())
                        .build();
            }
            userAnswerRepository.save(answer);
        }
        log.info("Successfully submitted {} answers for attempt: {}", request.getAnswers().size(), attemptId);
    }

    @Override
    @Transactional
    public AutoScoreResponse autoScore(Long attemptId) {
        log.info("Auto-scoring attempt: {}", attemptId);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!"PENDING".equals(attempt.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING attempts can be auto-scored");
        }

        List<UserAnswer> answers = userAnswerRepository.findByAttemptId(attemptId);
        if (answers.isEmpty()) {
            return AutoScoreResponse.builder().score(BigDecimal.ZERO).build();
        }

        int correctCount = 0;
        for (UserAnswer answer : answers) {
            // Simple mock logic: if selectedOption is "correct", mark as correct
            boolean isCorrect = "correct".equalsIgnoreCase(answer.getSelectedOption());
            answer.setIsCorrect(isCorrect);
            if (isCorrect) {
                correctCount++;
            }
            userAnswerRepository.save(answer);
        }
        
        // Calculate score (simple: correct answers / total answers * 10)
        BigDecimal score = BigDecimal.valueOf(correctCount)
                .divide(BigDecimal.valueOf(answers.size()), 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN);
        
        log.info("Auto-scored attempt {}: {}/{} correct, score: {}", attemptId, correctCount, answers.size(), score);
        
        return AutoScoreResponse.builder()
                .score(score)
                .build();
    }

    @Override
    @Transactional
    public void finalizeAttempt(Long attemptId, FinalizeAttemptRequest request) {
        log.info("Finalizing attempt: {} with score: {}", attemptId, request.getScore());
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!"PENDING".equals(attempt.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING attempts can be finalized");
        }

        attempt.setStatus("SUBMITTED");
        attempt.setFinishedAt(Instant.now());
        attempt.setScore(request.getScore());
        quizAttemptRepository.save(attempt);
        log.info("Successfully finalized attempt: {} with score: {}", attemptId, request.getScore());
    }
}
