package com.khoavdse170395.orchestratorservice;

import com.khoavdse170395.orchestratorservice.dto.StartAttemptRequest;
import com.khoavdse170395.orchestratorservice.dto.StartAttemptResponse;
import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class OrchestratorServiceApplicationTests {

    @Test
    public void testStartAttemptRequestCreation() {
        StartAttemptRequest request = StartAttemptRequest.builder()
                .assignmentId(1L)
                .build();
        
        assertNotNull(request);
        assertEquals(1L, request.getAssignmentId());
    }

    @Test
    public void testSubmitAttemptRequestCreation() {
        SubmitAttemptRequest.AnswerDto answer = SubmitAttemptRequest.AnswerDto.builder()
                .questionId(1L)
                .selectedOption("correct")
                .build();
        
        SubmitAttemptRequest request = SubmitAttemptRequest.builder()
                .attemptId(1001L)
                .answers(Arrays.asList(answer))
                .build();
        
        assertNotNull(request);
        assertEquals(1001L, request.getAttemptId());
        assertEquals(1, request.getAnswers().size());
        assertEquals(1L, request.getAnswers().get(0).getQuestionId());
        assertEquals("correct", request.getAnswers().get(0).getSelectedOption());
    }

    @Test
    public void testStartAttemptResponseCreation() {
        StartAttemptResponse response = StartAttemptResponse.builder()
                .attemptId(1001L)
                .build();
        
        assertNotNull(response);
        assertEquals(1001L, response.getAttemptId());
    }
}