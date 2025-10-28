# 🚀 Hướng Dẫn Sử Dụng Project SAGA_DEMO

## 📋 **Tổng Quan**
Project này implement **SAGA Pattern** với 3 microservices:
- **Assignment Service** (Port 8081): Quản lý quiz assignments
- **Attempt Service** (Port 8082): Quản lý quiz attempts  
- **Orchestrator Service** (Port 8080): Điều phối SAGA workflows

## 🛠️ **Bước 1: Chuẩn Bị Môi Trường**

### **1.1. Yêu Cầu Hệ Thống:**
- Java 17+
- Maven 3.6+
- MS SQL Server
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### **1.2. Cài Đặt MS SQL Server:**
```bash
# Sử dụng Docker (khuyến nghị)
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=12345" \
   -p 1433:1433 --name sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

### **1.3. Tạo Database:**
Chạy script `setup-database.sql` để tạo:
- `assignment_db` - Database cho assignment service
- `attempt_db` - Database cho attempt service
- Các bảng và dữ liệu mẫu

## 🚀 **Bước 2: Khởi Động Các Service**

### **2.1. Assignment Service (Port 8081):**
```bash
cd assignment-service
mvn clean compile
mvn spring-boot:run
```

**Swagger UI:** http://localhost:8081/swagger-ui.html

### **2.2. Attempt Service (Port 8082):**
```bash
cd ../attempt-service  
mvn clean compile
mvn spring-boot:run
```

**Swagger UI:** http://localhost:8082/swagger-ui.html

### **2.3. Orchestrator Service (Port 8080):**
```bash
cd ../orchestrator-service
mvn clean compile  
mvn spring-boot:run
```

**Swagger UI:** http://localhost:8080/swagger-ui.html

## 🧪 **Bước 3: Test API**

### **3.1. Test Assignment Service:**

#### **Check Eligibility:**
```bash
curl -X POST http://localhost:8081/assignments/1/check-eligibility \
  -H "Content-Type: application/json" \
  -d '{"userId": "demo-user"}'
```

#### **Reserve Attempt:**
```bash
curl -X POST http://localhost:8081/assignments/1/reserve \
  -H "Content-Type: application/json" \
  -d '{"userId": "demo-user", "idempotencyKey": "uuid-123"}'
```

### **3.2. Test Attempt Service:**

#### **Create Attempt:**
```bash
curl -X POST http://localhost:8082/attempts \
  -H "Content-Type: application/json" \
  -d '{"assignmentId": 1, "userId": "demo-user", "idempotencyKey": "uuid-123"}'
```

#### **Submit Answers:**
```bash
curl -X POST http://localhost:8082/attempts/1001/answers \
  -H "Content-Type: application/json" \
  -d '{"answers": [{"questionId": 1, "selectedOption": "correct"}]}'
```

#### **Auto-Score:**
```bash
curl -X POST http://localhost:8082/attempts/1001/autoscore
```

#### **Finalize:**
```bash
curl -X POST http://localhost:8082/attempts/1001/finalize \
  -H "Content-Type: application/json" \
  -d '{"score": 10.0}'
```

### **3.3. Test Orchestrator Service (SAGA):**

#### **Start Attempt SAGA:**
```bash
curl -X POST http://localhost:8080/saga/start-attempt \
  -H "Content-Type: application/json" \
  -d '{"assignmentId": 1}'
```

#### **Submit Attempt SAGA:**
```bash
curl -X POST http://localhost:8080/saga/submit-attempt \
  -H "Content-Type: application/json" \
  -d '{"attemptId": 1001, "answers": [{"questionId": 1, "selectedOption": "correct"}]}'
```

## 📊 **Bước 4: Sử Dụng Swagger UI**

### **4.1. Truy Cập Swagger UI:**
- Assignment Service: http://localhost:8081/swagger-ui.html
- Attempt Service: http://localhost:8082/swagger-ui.html  
- Orchestrator Service: http://localhost:8080/swagger-ui.html

### **4.2. Test Workflow Hoàn Chỉnh:**

#### **Scenario 1: Happy Path**
1. Mở Swagger UI Orchestrator Service
2. Test `/saga/start-attempt` với `{"assignmentId": 1}`
3. Lấy `attemptId` từ response
4. Test `/saga/submit-attempt` với `attemptId` và answers
5. Kiểm tra kết quả trong database

#### **Scenario 2: Compensation Test**
1. Tạm thời stop Assignment Service
2. Test `/saga/start-attempt` 
3. Xem compensation logic hoạt động
4. Restart Assignment Service

## 🔍 **Bước 5: Kiểm Tra Database**

### **5.1. Assignment Database:**
```sql
USE assignment_db;
SELECT * FROM quiz_assignment;
SELECT * FROM assignment_attempt_counter;
```

### **5.2. Attempt Database:**
```sql
USE attempt_db;
SELECT * FROM quiz_attempt;
SELECT * FROM user_answer;
```

## 🐛 **Bước 6: Debug và Troubleshooting**

### **6.1. Kiểm Tra Logs:**
- Mở console của từng service để xem logs
- Logs sẽ hiển thị chi tiết SAGA flow và compensation

### **6.2. Common Issues:**

#### **Database Connection Error:**
- Kiểm tra MS SQL Server đang chạy
- Kiểm tra connection string trong `application.properties`
- Kiểm tra username/password

#### **Port Already in Use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac  
lsof -ti:8080 | xargs kill -9
```

#### **Service Not Found:**
- Đảm bảo tất cả services đang chạy
- Kiểm tra port configuration
- Kiểm tra network connectivity

## 📈 **Bước 7: Monitoring**

### **7.1. Health Checks:**
- Assignment Service: http://localhost:8081/actuator/health
- Attempt Service: http://localhost:8082/actuator/health
- Orchestrator Service: http://localhost:8080/actuator/health

### **7.2. Metrics:**
- Assignment Service: http://localhost:8081/actuator/metrics
- Attempt Service: http://localhost:8082/actuator/metrics
- Orchestrator Service: http://localhost:8080/actuator/metrics

## 🎯 **Bước 8: Advanced Testing**

### **8.1. Load Testing:**
```bash
# Sử dụng Apache Bench
ab -n 100 -c 10 -H "Content-Type: application/json" \
   -p start-attempt.json http://localhost:8080/saga/start-attempt
```

### **8.2. Idempotency Testing:**
```bash
# Gọi cùng một request nhiều lần với cùng idempotencyKey
curl -X POST http://localhost:8081/assignments/1/reserve \
  -H "Content-Type: application/json" \
  -d '{"userId": "demo-user", "idempotencyKey": "same-key"}'
```

## 📚 **Bước 9: Hiểu Rõ SAGA Pattern**

### **9.1. Start Attempt SAGA Flow:**
```
1. Check Eligibility (Assignment Service)
2. Create Attempt (Attempt Service) 
3. Reserve Attempt (Assignment Service)
4. Return attemptId

Compensation (nếu có lỗi):
- Cancel Attempt
- Release Assignment Reservation
```

### **9.2. Submit Attempt SAGA Flow:**
```
1. Submit Answers (Attempt Service)
2. Auto-Score (Attempt Service)
3. Finalize Attempt (Attempt Service)
```

## 🔧 **Bước 10: Customization**

### **10.1. Thay Đổi Configuration:**
- Sửa `application.properties` trong từng service
- Thay đổi database connection
- Thay đổi port numbers
- Thay đổi timeout settings

### **10.2. Thêm Business Logic:**
- Sửa scoring algorithm trong `AttemptServiceImpl`
- Thêm validation rules
- Thêm custom compensation logic

## ✅ **Kết Luận**

Project SAGA_DEMO cung cấp:
- ✅ Complete SAGA Pattern implementation
- ✅ Swagger UI documentation
- ✅ Comprehensive error handling
- ✅ Idempotency support
- ✅ Transaction management
- ✅ Compensation logic
- ✅ Detailed logging

Bạn có thể sử dụng project này để:
- Học SAGA Pattern
- Test microservices architecture
- Develop quiz management system
- Understand distributed transactions

