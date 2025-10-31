package com.khoavdse170395.attemptservice.repository;

import com.khoavdse170395.attemptservice.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    Optional<QuizAttempt> findByIdempotencyKey(String idempotencyKey);
    
    List<QuizAttempt> findByAssignmentIdAndUserId(Long assignmentId, String userId);
    
    List<QuizAttempt> findByUserId(String userId);
    
    List<QuizAttempt> findByStatus(String status);
}





