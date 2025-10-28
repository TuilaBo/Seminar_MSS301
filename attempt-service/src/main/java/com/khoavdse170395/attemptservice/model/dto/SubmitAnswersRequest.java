package com.khoavdse170395.attemptservice.model.dto;

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
public class SubmitAnswersRequest {
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
