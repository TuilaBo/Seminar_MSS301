package com.khoavdse170395.attemptservice.controller;

import com.khoavdse170395.attemptservice.model.dto.*;
import com.khoavdse170395.attemptservice.service.AttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/attempts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attempt Management", description = "APIs for managing quiz attempts and user answers")
public class AttemptController {

    private final AttemptService attemptService;

    @Operation(summary = "Create attempt", description = "Create a new quiz attempt (idempotent)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attempt created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Attempt already exists with same idempotency key")
    })
    @PostMapping
    public ResponseEntity<CreateAttemptResponse> createAttempt(@Valid @RequestBody CreateAttemptRequest request) {
        log.info("Received create attempt request: {}", request);
        
        CreateAttemptResponse response = attemptService.createAttempt(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cancel attempt", description = "Cancel a pending quiz attempt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attempt canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Attempt cannot be canceled"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    @PostMapping("/{attemptId}/cancel")
    public ResponseEntity<Void> cancelAttempt(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId) {
        log.info("Received cancel attempt request for attemptId: {}", attemptId);
        
        attemptService.cancelAttempt(attemptId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Submit answers", description = "Submit user answers for a quiz attempt (upsert)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Answers submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or attempt not pending"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    @PostMapping("/{attemptId}/answers")
    public ResponseEntity<Void> submitAnswers(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId, 
            @Valid @RequestBody SubmitAnswersRequest request) {
        log.info("Received submit answers request for attemptId: {}, answers count: {}", 
                attemptId, request.getAnswers().size());
        
        attemptService.submitAnswers(attemptId, request);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Auto-score attempt", description = "Automatically score a quiz attempt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auto-scoring completed"),
            @ApiResponse(responseCode = "400", description = "Attempt cannot be auto-scored"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    @PostMapping("/{attemptId}/autoscore")
    public ResponseEntity<AutoScoreResponse> autoScore(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId) {
        log.info("Received auto-score request for attemptId: {}", attemptId);
        
        AutoScoreResponse response = attemptService.autoScore(attemptId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Finalize attempt", description = "Finalize a quiz attempt with final score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attempt finalized successfully"),
            @ApiResponse(responseCode = "400", description = "Attempt cannot be finalized"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    @PostMapping("/{attemptId}/finalize")
    public ResponseEntity<Void> finalizeAttempt(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId, 
            @Valid @RequestBody FinalizeAttemptRequest request) {
        log.info("Received finalize attempt request for attemptId: {}, score: {}", 
                attemptId, request.getScore());
        
        attemptService.finalizeAttempt(attemptId, request);
        
        return ResponseEntity.noContent().build();
    }
}
