package com.khoavdse170395.attemptservice.service;

import com.khoavdse170395.attemptservice.model.dto.AutoScoreResponse;
import com.khoavdse170395.attemptservice.model.dto.CreateAttemptRequest;
import com.khoavdse170395.attemptservice.model.dto.CreateAttemptResponse;
import com.khoavdse170395.attemptservice.model.dto.FinalizeAttemptRequest;
import com.khoavdse170395.attemptservice.model.dto.SubmitAnswersRequest;

public interface AttemptService {
    CreateAttemptResponse createAttempt(CreateAttemptRequest request);
    void cancelAttempt(Long attemptId);
    void submitAnswers(Long attemptId, SubmitAnswersRequest request);
    AutoScoreResponse autoScore(Long attemptId);
    void finalizeAttempt(Long attemptId, FinalizeAttemptRequest request);
}





