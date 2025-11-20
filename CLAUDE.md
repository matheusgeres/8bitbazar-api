# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## User Preferences

- Respond in Brazilian Portuguese (PT-BR)

## Project Overview

8BitBazar is a REST API for a retro gaming marketplace that combines e-commerce with auction functionality. Built with Java 21 and Spring Boot 3.2.x.

## Build & Run Commands

```bash
# Start infrastructure (MySQL, Elasticsearch, MinIO, RabbitMQ, Liquibase)
podman compose up -d

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run single test class
./gradlew test --tests "com.eightbitbazar.SomeTest"

# Run single test method
./gradlew test --tests "com.eightbitbazar.SomeTest.testMethod"

# Run tests with Podman (for Testcontainers)
./gradlew test -Dspring.profiles.active=local-podman
```

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters):

```
src/main/java/com/eightbitbazar/
├── domain/              # Entities, value objects, enums (pure business logic)
├── application/
│   ├── port/
│   │   ├── in/          # Use case interfaces (input ports)
│   │   └── out/         # Repository interfaces (output ports)
│   └── usecase/         # Use case implementations with Input/Output DTOs
├── adapter/
│   ├── in/web/          # REST controllers with Request/Response DTOs
│   └── out/
│       ├── persistence/ # JPA entities, repositories, mappers
│       ├── storage/     # MinIO adapter
│       ├── search/      # Elasticsearch adapter
│       └── messaging/   # RabbitMQ adapter
└── config/              # Spring configuration classes
```

### Key Architectural Patterns

- **Three-layer DTOs**: Web (Request/Response) → Application (Input/Output) → Domain (Entities)
- **Soft Delete**: All entities use logical deletion (`deleted` flag)
- **Immutability**: Domain objects are immutable where possible
- **Use Cases**: Each business action has its own use case class

### Domain Entities

- **User**: With Address embedded, supports seller role
- **Listing**: Products with types (AUCTION, DIRECT_SALE, SHOWCASE)
- **Bid**: For auction listings
- **Purchase**: Direct sales or auction wins
- **Platform/Manufacturer**: Admin-managed reference data

## Tech Stack

- **Auth**: Spring Authorization Server (OAuth2/OIDC, self-hosted)
- **Database**: MySQL 8.0 with Liquibase migrations (in `liquibase/changelog/`)
- **Search**: Elasticsearch 8.11
- **Storage**: MinIO (S3-compatible)
- **Messaging**: RabbitMQ
- **Testing**: JUnit 5 with Testcontainers

## Database

Liquibase manages migrations in `liquibase/changelog/`. The `db.changelog-master.xml` is the entry point. Migrations run automatically via docker-compose.

## Authentication

Uses OAuth2 Password Grant:
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -d "grant_type=password&username=email&password=pass&client_id=eightbitbazar-web"
```
