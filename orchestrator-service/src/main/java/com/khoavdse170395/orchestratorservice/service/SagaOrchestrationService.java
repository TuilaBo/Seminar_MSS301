package com.khoavdse170395.orchestratorservice.service;

import com.khoavdse170395.orchestratorservice.dto.StartAttemptRequest;
import com.khoavdse170395.orchestratorservice.dto.StartAttemptResponse;
import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;

public interface SagaOrchestrationService {
    StartAttemptResponse startAttempt(StartAttemptRequest request);
    void submitAttempt(SubmitAttemptRequest request);
}