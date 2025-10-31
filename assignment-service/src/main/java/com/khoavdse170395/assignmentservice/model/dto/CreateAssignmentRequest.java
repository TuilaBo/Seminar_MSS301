package com.khoavdse170395.assignmentservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {

    @NotNull(message = "quizId is required")
    private Long quizId;

    private String allowedGroup;

    // Có thể null nếu mở ngay
    private Instant openAt;

    // Có thể null nếu không giới hạn
    private Instant closeAt;

    @NotNull
    @Min(value = 1, message = "maxAttempts must be >= 1")
    private Integer maxAttempts;
}


