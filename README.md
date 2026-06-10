# Real-Time GPS Tracking Service

A highly scalable Spring Boot microservice designed to ingest, process, and store real-time GPS coordinates using an event-driven architecture.

## Architecture
* **Framework:** Spring Boot (Java 17)
* **Message Broker:** RabbitMQ (for asynchronous GPS event ingestion)
* **Database:** MySQL
* **Infrastructure:** Docker Compose

## Quick Start (Run Locally)
You don't need to install MySQL or RabbitMQ locally. Just use Docker

1. **Start the infrastructure (DB & Message Broker):**
   ```bash
   docker-compose up -d