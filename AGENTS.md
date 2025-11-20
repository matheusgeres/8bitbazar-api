# Repository Guidelines

Guide for agents contributing to 8BitBazar’s API codebase. Preferir respostas e comunicações em PT-BR sempre que possível.

## Project Structure & Module Organization
- Java sources live in `src/main/java/com/eightbitbazar`, following Hexagonal Architecture: `domain` (business objects), `application/port` and `application/usecase` (input/output ports and use cases), `adapter/in` (web controllers) and `adapter/out` (persistence, storage, search, messaging). Configuration lives in `config`.
- Tests mirror the main packages under `src/test/java`. Shared fixtures and test config sit in `src/test/resources` (e.g., `application-test.yml`, `testcontainers.properties`).
- Application configuration is in `src/main/resources/application.yml`. Database migrations reside in `liquibase/` and are applied by the compose stack.

## Build, Test, and Development Commands
- `podman compose up -d` (or `docker compose up -d`): boots MySQL, Elasticsearch, MinIO, RabbitMQ, and Liquibase. Required before running the app locally.
- `./gradlew clean build`: compiles and runs the full test suite.
- `./gradlew test`: runs JUnit/Testcontainers tests; use `-Dspring.profiles.active=local-podman` when running rootless Podman so Testcontainers picks up the socket.
- `./gradlew bootRun`: starts the API on `http://localhost:8080` using local configs.

## Coding Style & Naming Conventions
- Java 21 + Spring Boot 3.2; use 4-space indentation, braces on the same line, and meaningful, immutable domain models where practical.
- Package naming stays lowercase; classes and records use PascalCase; request/response DTOs end with `Request`/`Response`; ports follow `XUseCase`/`XPort` naming to reflect intent.
- Prefer constructor injection; rely on Lombok only for boilerplate where already used. Keep adapters thin and push business logic into use cases/domain.

## Testing Guidelines
- JUnit 5 + Spring Boot Test + Testcontainers (MySQL, RabbitMQ, Elasticsearch, MinIO). Test settings live in `src/test/resources/application-test.yml`; containers are reused via `testcontainers.properties`.
- Place tests alongside their production packages and suffix with `*Test`. Keep integration tests isolated and idempotent; `maxParallelForks=1` is already set to avoid container clashes.
- For Podman users, run `./gradlew test -Dspring.profiles.active=local-podman` to auto-export `DOCKER_HOST`.

## Commit & Pull Request Guidelines
- Use concise, imperative commit messages; Conventional Commit prefixes (`feat:`, `fix:`, `chore:`, `test:`, `docs:`) are preferred for readability.
- PRs should summarize scope, list modified endpoints/use cases, note schema or Liquibase changes, and include the test command executed (`./gradlew test` or `./gradlew clean build`). Provide sample requests/responses when adding or changing APIs.

## Security & Configuration Tips
- Do not commit credentials; the defaults in `application.yml` and compose are dev-only. Override via environment variables when needed.
- When altering persistence, add/update Liquibase change sets under `liquibase/` and ensure the compose stack applies them cleanly.
- Keep OAuth client settings in sync between `SecurityConfig` and consumer apps to avoid login breakage.
