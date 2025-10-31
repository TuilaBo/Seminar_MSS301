package com.khoavdse170395.orchestratorservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAttemptRequest {
    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
    
    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerDto> answers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        private String selectedOption;
        
        private String answerText;
    }
}





