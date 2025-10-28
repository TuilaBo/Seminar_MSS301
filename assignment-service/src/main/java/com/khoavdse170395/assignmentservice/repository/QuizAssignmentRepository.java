package com.khoavdse170395.assignmentservice.repository;

import com.khoavdse170395.assignmentservice.model.QuizAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {
    
    Optional<QuizAssignment> findByAssignmentId(Long assignmentId);
    
    Optional<QuizAssignment> findByQuizId(Long quizId);
}

