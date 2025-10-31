package com.khoavdse170395.assignmentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentResponse {
    private Long assignmentId;
    private Integer maxAttempts;
}





