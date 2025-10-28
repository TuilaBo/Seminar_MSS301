# Orchestrator Service - SAGA Pattern Implementation

## Overview
The orchestrator-service implements HTTP orchestration with SAGA pattern compensation for managing quiz attempts across assignment-service and attempt-service.

## Configuration
- **Port**: 8080
- **Assignment Service**: http://localhost:8081
- **Attempt Service**: http://localhost:8082
- **Connect Timeout**: 3000ms
- **Read Timeout**: 5000ms

## API Endpoints

### 1. Start Attempt SAGA
**POST** `/saga/start-attempt`

**Request:**
```json
{
  "assignmentId": 1
}
```

**Response:**
```json
{
  "attemptId": 1001
}
```

**SAGA Flow:**
1. Check eligibility with assignment-service
2. Create attempt with attempt-service (idempotent)
3. Reserve attempt with assignment-service
4. **Compensation**: If any step fails, cancel attempt and release reservation

### 2. Submit Attempt SAGA
**POST** `/saga/submit-attempt`

**Request:**
```json
{
  "attemptId": 1001,
  "answers": [
    {
      "questionId": 1,
      "selectedOption": "correct",
      "answerText": null
    }
  ]
}
```

**SAGA Flow:**
1. Submit answers to attempt-service
2. Auto-score the attempt
3. Finalize attempt with score

## Testing Instructions

### Prerequisites
1. Start assignment-service on port 8081
2. Start attempt-service on port 8082
3. Ensure databases are running (assignment_db, attempt_db)

### Test Data Setup
Create a quiz assignment in assignment_db:
```sql
INSERT INTO quiz_assignment (assignment_id, quiz_id, allowed_group, open_at, close_at, max_attempts, created_at)
VALUES (1, 99, 'demo-group', DATEADD(hour, -1, GETUTCDATE()), DATEADD(hour, 1, GETUTCDATE()), 1, GETUTCDATE());
```

### Happy Path Test
1. Start orchestrator-service: `mvn spring-boot:run`
2. Test start attempt:
```bash
curl -X POST http://localhost:8080/saga/start-attempt \
  -H "Content-Type: application/json" \
  -d '{"assignmentId": 1}'
```
3. Test submit attempt:
```bash
curl -X POST http://localhost:8080/saga/submit-attempt \
  -H "Content-Type: application/json" \
  -d '{"attemptId": 1001, "answers": [{"questionId": 1, "selectedOption": "correct"}]}'
```

### Compensation Test
To test compensation logic, temporarily modify assignment-service to throw an exception during reserve operation. The orchestrator will:
1. Cancel the created attempt
2. Release the assignment reservation (best effort)

## Features
- **Idempotency**: Uses UUID-based idempotency keys
- **Compensation**: Automatic rollback on failures
- **Timeout Handling**: Configurable HTTP timeouts
- **Error Handling**: Graceful failure with compensation
- **Logging**: Comprehensive debug logging
- **Validation**: Input validation with Jakarta validation

## Architecture
- **Controller**: REST endpoints (`SagaController`)
- **Service**: SAGA orchestration logic (`SagaOrchestrationService`)
- **Client**: HTTP clients for external services (`AssignmentServiceClient`, `AttemptServiceClient`)
- **DTO**: Request/Response objects
- **Config**: RestClient configuration

## Dependencies
- Spring Boot Web
- Spring Boot Validation
- Spring Boot Actuator
- Lombok
- Resilience4j (optional)

