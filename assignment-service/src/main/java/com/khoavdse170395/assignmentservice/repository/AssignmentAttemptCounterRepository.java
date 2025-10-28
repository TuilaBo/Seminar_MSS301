package com.khoavdse170395.assignmentservice.repository;

import com.khoavdse170395.assignmentservice.model.AssignmentAttemptCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AssignmentAttemptCounterRepository extends JpaRepository<AssignmentAttemptCounter, AssignmentAttemptCounter.AssignmentAttemptCounterId> {
    
    Optional<AssignmentAttemptCounter> findByAssignmentIdAndUserId(Long assignmentId, String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE AssignmentAttemptCounter a SET a.used = a.used + 1 WHERE a.assignmentId = :assignmentId AND a.userId = :userId")
    int incrementUsedAttempts(@Param("assignmentId") Long assignmentId, @Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE AssignmentAttemptCounter a SET a.used = a.used - 1 WHERE a.assignmentId = :assignmentId AND a.userId = :userId AND a.used > 0")
    int decrementUsedAttempts(@Param("assignmentId") Long assignmentId, @Param("userId") String userId);
}
