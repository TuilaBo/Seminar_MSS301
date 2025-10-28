package com.khoavdse170395.assignmentservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
