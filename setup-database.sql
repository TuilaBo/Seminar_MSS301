-- Tạo database cho assignment-service
CREATE DATABASE assignment_db;
GO

-- Tạo database cho attempt-service  
CREATE DATABASE attempt_db;
GO

-- Sử dụng assignment_db
USE assignment_db;
GO

-- Tạo bảng quiz_assignment
CREATE TABLE quiz_assignment (
    assignment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    allowed_group NVARCHAR(255) NULL,
    open_at DATETIME2 NULL,
    close_at DATETIME2 NULL,
    max_attempts INT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

-- Tạo bảng assignment_attempt_counter
CREATE TABLE assignment_attempt_counter (
    assignment_id BIGINT NOT NULL,
    user_id NVARCHAR(200) NOT NULL,
    used INT NOT NULL DEFAULT 0,
    PRIMARY KEY (assignment_id, user_id)
);

-- Sử dụng attempt_db
USE attempt_db;
GO

-- Tạo bảng quiz_attempt
CREATE TABLE quiz_attempt (
    attempt_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    user_id NVARCHAR(200) NOT NULL,
    status NVARCHAR(50) NOT NULL,
    started_at DATETIME2 NULL,
    finished_at DATETIME2 NULL,
    score DECIMAL(5,2) NULL,
    idempotency_key NVARCHAR(100) UNIQUE NULL
);

-- Tạo unique index cho idempotency_key
CREATE UNIQUE INDEX ux_attempt_idempotency_key ON quiz_attempt(idempotency_key);

-- Tạo bảng user_answer
CREATE TABLE user_answer (
    answer_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option NVARCHAR(50) NULL,
    answer_text NVARCHAR(MAX) NULL,
    is_correct BIT NULL,
    UNIQUE(attempt_id, question_id)
);

-- Thêm dữ liệu mẫu
USE assignment_db;
GO

INSERT INTO quiz_assignment (quiz_id, allowed_group, open_at, close_at, max_attempts, created_at)
VALUES (99, 'demo-group', DATEADD(hour, -1, GETUTCDATE()), DATEADD(hour, 1, GETUTCDATE()), 1, GETUTCDATE());

SELECT * FROM quiz_assignment;

