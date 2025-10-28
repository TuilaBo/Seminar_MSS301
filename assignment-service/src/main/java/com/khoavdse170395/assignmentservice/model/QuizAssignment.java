package com.khoavdse170395.assignmentservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "quiz_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    // Group hoặc class cho phép làm quiz này (từ claim JWT)
    @Column(name = "allowed_group", length = 255)
    private String allowedGroup;

    @Column(name = "open_at")
    private Instant openAt;

    @Column(name = "close_at")
    private Instant closeAt;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}