#!/bin/bash
echo "Starting SAGA Demo Services..."

echo ""
echo "Starting Assignment Service (Port 8081)..."
gnome-terminal -- bash -c "cd assignment-service && mvn clean package -DskipTests && java -jar target/assignment-service-0.0.1-SNAPSHOT.jar; exec bash"

sleep 5

echo ""
echo "Starting Attempt Service (Port 8082)..."
gnome-terminal -- bash -c "cd attempt-service && mvn clean package -DskipTests && java -jar target/attempt-service-0.0.1-SNAPSHOT.jar; exec bash"

sleep 5

echo ""
echo "Starting Orchestrator Service (Port 8080)..."
gnome-terminal -- bash -c "cd orchestrator-service && mvn clean package -DskipTests && java -jar target/orchestrator-service-0.0.1-SNAPSHOT.jar; exec bash"

echo ""
echo "All services are starting..."
echo ""
echo "Swagger UI URLs:"
echo "- Assignment Service: http://localhost:8081/swagger-ui.html"
echo "- Attempt Service: http://localhost:8082/swagger-ui.html"
echo "- Orchestrator Service: http://localhost:8080/swagger-ui.html"
echo ""
echo "Test Assignments:"
echo "- Assignment ID 1: max_attempts = 1 (expired)"
echo "- Assignment ID 2: max_attempts = 3 (active)"
echo "- Assignment ID 3: max_attempts = 5 (active)"
echo ""
read -p "Press Enter to continue..."
