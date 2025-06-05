# GPS Data Processing Service

A Spring Boot backend application demonstrating an asynchronous system for ingesting, storing, querying, and managing GPS data. Features include RESTful APIs, RabbitMQ for message queuing to enhance API responsiveness, MySQL for data persistence via Spring Data JPA/Hibernate, and a scheduled task for automated data retention.

**Key Features:**
*   Asynchronous GPS data ingestion via RabbitMQ.
*   RESTful APIs for data submission and retrieval (all records, by publisher).
*   Data persistence in MySQL using Spring Data JPA.
*   Scheduled daily task for purging old data based on a configurable retention period.
*   Local RabbitMQ setup managed with Docker.
*   Added the feature of Unit Testing for the(GpsServiceImplmn) class.

**Technologies Used:**
Java, Spring Boot (Spring MVC, Spring Data JPA, Spring AMQP, Spring Scheduling, JUnit5, Mockito), RabbitMQ, MySQL, Hibernate, Jackson, Lombok, Docker.
