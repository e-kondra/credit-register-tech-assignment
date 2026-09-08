# Credit Register POC

## Description
Proof-of-concept system for getting from Positive Credit Register (PCR) with voluntary credit ban monitoring.

## Architecture
- **Backend API** (Spring Boot 3.2.4) — Integration with PCR, storing data, REST API
- **Monitoring Service** (Spring Boot 3.2.4) — Kafka consumer, email notification
- **Frontend** (React + Vite) — UI
- **Kafka** — Asynchronous transmission of credit bank events
- **PostgreSQL** — Data Base

## Technologies
- Java 21 / Spring Boot 3.2.4
- React 18 / Vite
- Docker / Docker Compose
- Apache Kafka (KRaft)
- PostgreSQL 16
- Maven

## Execution

### Requirements
- Docker & Docker Compose
- Java 21 
- Maven 3.9+

### Services
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Monitoring Service: http://localhost:8082
- PostgreSQL: localhost:5432
- Kafka: localhost:9092