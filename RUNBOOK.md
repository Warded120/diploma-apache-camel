# Runbook: Docker Compose for Spring Boot Application

## Prerequisites

- Docker and Docker Compose installed
- Maven build completed (`mvn clean package`)

## Steps

1. **Build Docker Images**
   ```sh
   docker compose build
   ```

2. **Start Services**
   ```sh
   docker compose up -d
   ```

3. **Test application**
   ```sh
   POST localhost:8081/orders
   ```

   ```json
   {
     "name": "apple",
     "quantity": 1,
     "price": 5.00
   }
   ```
