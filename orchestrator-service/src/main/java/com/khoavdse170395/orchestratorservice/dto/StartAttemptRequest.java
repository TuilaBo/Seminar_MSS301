package com.khoavdse170395.orchestratorservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartAttemptRequest {
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
}

