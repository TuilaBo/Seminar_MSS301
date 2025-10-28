package com.khoavdse170395.attemptservice.repository;

import com.khoavdse170395.attemptservice.model.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    List<UserAnswer> findByAttemptId(Long attemptId);
    
    Optional<UserAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
    
    @Modifying
    @Query("UPDATE UserAnswer u SET u.selectedOption = :selectedOption, u.answerText = :answerText WHERE u.attemptId = :attemptId AND u.questionId = :questionId")
    int updateAnswer(@Param("attemptId") Long attemptId, 
                     @Param("questionId") Long questionId,
                     @Param("selectedOption") String selectedOption,
                     @Param("answerText") String answerText);
    
    @Query("SELECT COUNT(u) FROM UserAnswer u WHERE u.attemptId = :attemptId AND u.isCorrect = true")
    long countCorrectAnswers(@Param("attemptId") Long attemptId);
    
    @Query("SELECT COUNT(u) FROM UserAnswer u WHERE u.attemptId = :attemptId")
    long countTotalAnswers(@Param("attemptId") Long attemptId);
}

