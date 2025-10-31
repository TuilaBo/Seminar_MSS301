package com.khoavdse170395.assignmentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemainingAttemptsResponse {
    private int remainingAttempts;
}



