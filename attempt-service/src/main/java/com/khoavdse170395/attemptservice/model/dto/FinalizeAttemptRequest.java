package com.khoavdse170395.attemptservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeAttemptRequest {
    @NotNull(message = "Score is required")
    private BigDecimal score;
}
