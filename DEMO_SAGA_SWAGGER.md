# 🎯 **Hướng Dẫn Demo SAGA Pattern với Swagger UI**

## 🚀 **Bước 1: Khởi Động Các Service**

### **1.1. Mở 3 Terminal Windows:**

**Terminal 1 - Assignment Service:**
```bash
cd assignment-service
mvn spring-boot:run
```
✅ **Kết quả:** Service chạy trên port 8081

**Terminal 2 - Attempt Service:**
```bash
cd attempt-service  
mvn spring-boot:run
```
✅ **Kết quả:** Service chạy trên port 8082

**Terminal 3 - Orchestrator Service:**
```bash
cd orchestrator-service
mvn spring-boot:run
```
✅ **Kết quả:** Service chạy trên port 8080

### **1.2. Kiểm Tra Services Đang Chạy:**
- Assignment Service: http://localhost:8081/actuator/health
- Attempt Service: http://localhost:8082/actuator/health  
- Orchestrator Service: http://localhost:8080/actuator/health

## 🌐 **Bước 2: Truy Cập Swagger UI**

### **2.1. Mở 3 Tab Browser:**

**Tab 1 - Assignment Service Swagger:**
```
http://localhost:8081/swagger-ui.html
```

**Tab 2 - Attempt Service Swagger:**
```
http://localhost:8082/swagger-ui.html
```

**Tab 3 - Orchestrator Service Swagger:**
```
http://localhost:8080/swagger-ui.html
```

### **2.2. Quan Sát API Documentation:**
- **Assignment Service:** 3 endpoints (check-eligibility, reserve, release)
- **Attempt Service:** 5 endpoints (create, cancel, answers, autoscore, finalize)
- **Orchestrator Service:** 2 endpoints (start-attempt, submit-attempt)

## 🎭 **Bước 3: Demo SAGA Pattern - Scenario 1: Happy Path**

### **3.1. Sử dụng Orchestrator Service Swagger:**

#### **Step 1: Start Attempt SAGA**
1. **Mở Tab Orchestrator Service Swagger**
2. **Tìm endpoint:** `POST /saga/start-attempt`
3. **Click "Try it out"**
4. **Nhập Request Body:**
```json
{
  "assignmentId": 1
}
```
5. **Click "Execute"**
6. **Quan sát Response:**
```json
{
  "attemptId": 1001,
  "status": "PENDING"
}
```

#### **Step 2: Submit Attempt SAGA**
1. **Sử dụng attemptId từ Step 1** (ví dụ: 1001)
2. **Tìm endpoint:** `POST /saga/submit-attempt`
3. **Click "Try it out"**
4. **Nhập Request Body:**
```json
{
  "attemptId": 1001,
  "answers": [
    {
      "questionId": 1,
      "selectedOption": "correct"
    },
    {
      "questionId": 2,
      "selectedOption": "correct"
    }
  ]
}
```
5. **Click "Execute"**
6. **Quan sát Response:** `200 OK`

### **3.2. Kiểm Tra Kết Quả trong Database:**

**Mở SQL Server Management Studio hoặc Azure Data Studio:**

```sql
-- Kiểm tra Assignment Database
USE assignment_db;
SELECT * FROM quiz_assignment;
SELECT * FROM assignment_attempt_counter;

-- Kiểm tra Attempt Database  
USE attempt_db;
SELECT * FROM quiz_attempt;
SELECT * FROM user_answer;
```

## 🔄 **Bước 4: Demo SAGA Pattern - Scenario 2: Compensation Logic**

### **4.1. Tạo Lỗi để Test Compensation:**

#### **Step 1: Stop Assignment Service**
1. **Vào Terminal 1** (Assignment Service)
2. **Nhấn Ctrl+C** để stop service
3. **Assignment Service sẽ không available**

#### **Step 2: Test Start Attempt với Service Down**
1. **Mở Orchestrator Service Swagger**
2. **Test endpoint:** `POST /saga/start-attempt`
3. **Nhập Request Body:**
```json
{
  "assignmentId": 1
}
```
4. **Click "Execute"**
5. **Quan sát Error Response:** `500 Internal Server Error`

#### **Step 3: Restart Assignment Service**
1. **Vào Terminal 1**
2. **Chạy lại:** `mvn spring-boot:run`
3. **Service sẽ restart trên port 8081**

#### **Step 4: Test Lại Start Attempt**
1. **Test lại endpoint:** `POST /saga/start-attempt`
2. **Quan sát:** SAGA sẽ thành công lần này

## 🔍 **Bước 5: Hiểu Rõ SAGA Flow**

### **5.1. Quan Sát Logs trong Terminal:**

**Trong Terminal 3 (Orchestrator Service), bạn sẽ thấy:**

```
INFO - Received start attempt request: StartAttemptRequest(assignmentId=1)
INFO - Checking eligibility for assignmentId=1, userId=demo-user
INFO - Creating attempt for assignmentId=1, userId=demo-user
INFO - Reserving attempt for assignmentId=1, userId=demo-user
INFO - Start attempt SAGA completed successfully: StartAttemptResponse(attemptId=1001, status=PENDING)
```

### **5.2. Quan Sát Logs trong Terminal 1 (Assignment Service):**

```
INFO - Checking eligibility for assignmentId=1, userId=demo-user
INFO - User demo-user is eligible for assignment 1
INFO - Reserving attempt for assignmentId=1, userId=demo-user
INFO - Attempt reserved successfully for assignmentId=1, userId=demo-user
```

### **5.3. Quan Sát Logs trong Terminal 2 (Attempt Service):**

```
INFO - Creating attempt for assignmentId=1, userId=demo-user
INFO - Attempt created successfully: QuizAttempt(attemptId=1001, status=PENDING)
```

## 🧪 **Bước 6: Test Idempotency**

### **6.1. Test Idempotency trong Attempt Service:**

1. **Mở Attempt Service Swagger**
2. **Tìm endpoint:** `POST /attempts`
3. **Test với cùng idempotencyKey:**

**Request 1:**
```json
{
  "assignmentId": 1,
  "userId": "demo-user",
  "idempotencyKey": "test-uuid-123"
}
```

**Request 2 (cùng idempotencyKey):**
```json
{
  "assignmentId": 1,
  "userId": "demo-user", 
  "idempotencyKey": "test-uuid-123"
}
```

4. **Quan sát:** Cả 2 request đều trả về cùng attemptId

### **6.2. Kiểm Tra Database:**

```sql
USE attempt_db;
SELECT * FROM quiz_attempt WHERE idempotency_key = 'test-uuid-123';
-- Chỉ có 1 record duy nhất
```

## 📊 **Bước 7: Test Individual Services**

### **7.1. Test Assignment Service Directly:**

1. **Mở Assignment Service Swagger**
2. **Test Check Eligibility:**
```json
{
  "userId": "demo-user"
}
```
3. **Test Reserve Attempt:**
```json
{
  "userId": "demo-user",
  "idempotencyKey": "direct-test-uuid"
}
```

### **7.2. Test Attempt Service Directly:**

1. **Mở Attempt Service Swagger**
2. **Test Create Attempt:**
```json
{
  "assignmentId": 1,
  "userId": "demo-user",
  "idempotencyKey": "direct-test-uuid"
}
```
3. **Test Submit Answers:**
```json
{
  "answers": [
    {
      "questionId": 1,
      "selectedOption": "correct"
    }
  ]
}
```

## 🎯 **Bước 8: Demo Workflow Hoàn Chỉnh**

### **8.1. Complete SAGA Demo:**

1. **Start Attempt SAGA** → Lấy attemptId
2. **Submit Attempt SAGA** → Hoàn thành quiz
3. **Kiểm tra Database** → Verify kết quả
4. **Test Compensation** → Stop service và test error handling
5. **Test Idempotency** → Cùng request nhiều lần

### **8.2. Quan Sát SAGA Pattern Benefits:**

- ✅ **Atomicity:** Tất cả steps thành công hoặc rollback
- ✅ **Consistency:** Data luôn consistent across services
- ✅ **Isolation:** Mỗi SAGA độc lập
- ✅ **Durability:** Kết quả được persist
- ✅ **Compensation:** Tự động rollback khi có lỗi

## 🔧 **Bước 9: Troubleshooting**

### **9.1. Common Issues:**

**Service không start:**
- Kiểm tra port đã được sử dụng
- Kiểm tra Java version (cần Java 17+)
- Kiểm tra Maven version

**Database connection error:**
- Kiểm tra MS SQL Server đang chạy
- Kiểm tra connection string
- Kiểm tra database đã được tạo

**Swagger UI không load:**
- Kiểm tra service đang chạy
- Kiểm tra port number
- Clear browser cache

### **9.2. Debug Tips:**

1. **Xem logs trong Terminal** để hiểu flow
2. **Kiểm tra database** để verify data
3. **Test từng service riêng lẻ** trước khi test SAGA
4. **Sử dụng Postman** nếu Swagger có vấn đề

## 📈 **Bước 10: Advanced Demo**

### **10.1. Load Testing:**

1. **Mở nhiều browser tabs**
2. **Test cùng lúc nhiều requests**
3. **Quan sát performance và error handling**

### **10.2. Error Scenarios:**

1. **Test với invalid assignmentId**
2. **Test với user không eligible**
3. **Test với answers không hợp lệ**
4. **Test timeout scenarios**

## ✅ **Kết Luận**

Sau khi hoàn thành các bước trên, bạn sẽ hiểu rõ:

- **SAGA Pattern** hoạt động như thế nào
- **Compensation logic** trong distributed systems
- **Idempotency** và tại sao quan trọng
- **Microservices communication** patterns
- **Error handling** trong distributed transactions

**Swagger UI** giúp bạn:
- ✅ Test API một cách trực quan
- ✅ Hiểu request/response format
- ✅ Debug và troubleshoot
- ✅ Demo cho stakeholders
- ✅ Document API behavior




