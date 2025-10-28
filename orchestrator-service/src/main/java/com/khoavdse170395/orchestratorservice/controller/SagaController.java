package com.khoavdse170395.orchestratorservice.controller;

import com.khoavdse170395.orchestratorservice.dto.StartAttemptRequest;
import com.khoavdse170395.orchestratorservice.dto.StartAttemptResponse;
import com.khoavdse170395.orchestratorservice.dto.SubmitAttemptRequest;
import com.khoavdse170395.orchestratorservice.service.SagaOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saga")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SAGA Orchestration", description = "SAGA Pattern APIs for orchestrating quiz attempt workflows")
public class SagaController {

    private final SagaOrchestrationService sagaOrchestrationService;

    @Operation(summary = "Start attempt SAGA", description = "Orchestrate the complete flow to start a quiz attempt with compensation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "SAGA started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not eligible"),
            @ApiResponse(responseCode = "500", description = "SAGA failed with compensation executed")
    })
    @PostMapping("/start-attempt")
    public ResponseEntity<StartAttemptResponse> startAttempt(@Valid @RequestBody StartAttemptRequest request) {
        log.info("Received start attempt request: {}", request);
        
        try {
            StartAttemptResponse response = sagaOrchestrationService.startAttempt(request);
            log.info("Start attempt SAGA completed successfully: {}", response);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            log.error("Start attempt SAGA failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Submit attempt SAGA", description = "Orchestrate the complete flow to submit and finalize a quiz attempt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SAGA completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or attempt not pending"),
            @ApiResponse(responseCode = "404", description = "Attempt not found"),
            @ApiResponse(responseCode = "500", description = "SAGA failed")
    })
    @PostMapping("/submit-attempt")
    public ResponseEntity<Void> submitAttempt(@Valid @RequestBody SubmitAttemptRequest request) {
        log.info("Received submit attempt request for attemptId: {}, answers count: {}", 
                request.getAttemptId(), request.getAnswers().size());
        
        try {
            sagaOrchestrationService.submitAttempt(request);
            log.info("Submit attempt SAGA completed successfully for attemptId: {}", request.getAttemptId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Submit attempt SAGA failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
