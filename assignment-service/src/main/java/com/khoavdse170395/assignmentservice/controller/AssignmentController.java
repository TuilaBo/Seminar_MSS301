package com.khoavdse170395.assignmentservice.controller;

import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityRequest;
import com.khoavdse170395.assignmentservice.model.dto.CheckEligibilityResponse;
import com.khoavdse170395.assignmentservice.model.dto.ReleaseRequest;
import com.khoavdse170395.assignmentservice.model.dto.ReserveRequest;
import com.khoavdse170395.assignmentservice.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assignment Management", description = "APIs for managing quiz assignments and attempt counters")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "Check user eligibility", description = "Check if a user is eligible to take a quiz assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligibility check completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/{assignmentId}/check-eligibility")
    public ResponseEntity<CheckEligibilityResponse> checkEligibility(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long assignmentId,
            @Valid @RequestBody CheckEligibilityRequest request) {
        
        log.info("Received check eligibility request for assignmentId: {}, userId: {}", 
                assignmentId, request.getUserId());
        
        CheckEligibilityResponse response = assignmentService.checkEligibility(assignmentId, request.getUserId());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reserve attempt", description = "Reserve a quiz attempt for a user (idempotent)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not eligible"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/{assignmentId}/reserve")
    public ResponseEntity<Void> reserveAttempt(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long assignmentId,
            @Valid @RequestBody ReserveRequest request) {
        
        log.info("Received reserve request for assignmentId: {}, userId: {}, idempotencyKey: {}", 
                assignmentId, request.getUserId(), request.getIdempotencyKey());
        
        assignmentService.reserveAttempt(assignmentId, request.getUserId(), request.getIdempotencyKey());
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Release attempt", description = "Release a reserved quiz attempt (for compensation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/{assignmentId}/release")
    public ResponseEntity<Void> releaseAttempt(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long assignmentId,
            @Valid @RequestBody ReleaseRequest request) {
        
        log.info("Received release request for assignmentId: {}, userId: {}, idempotencyKey: {}", 
                assignmentId, request.getUserId(), request.getIdempotencyKey());
        
        assignmentService.releaseAttempt(assignmentId, request.getUserId(), request.getIdempotencyKey());
        
        return ResponseEntity.ok().build();
    }
}
