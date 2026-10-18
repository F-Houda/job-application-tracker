# Job Application Tracker API

[![CI](https://github.com/F-Houda/job-application-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/F-Houda/job-application-tracker/actions/workflows/ci.yml)

A small REST API to track job applications and the history of their status changes.
Built with **Java 21, Spring Boot 3, PostgreSQL and Flyway**, tested against a real database with
**Testcontainers**, and shipped as a Docker image through **GitHub Actions**.

The domain is deliberately small. The point of the project is the engineering around it:
validated input, a state machine with enforced transitions, schema migrations, consistent error responses,
integration tests that exercise the real stack, and a CI/CD pipeline.

## Quick start

```bash
docker compose up --build
```

- API: http://localhost:8080/api/applications
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

```bash
# create an application (starts as WISHLIST)
curl -X POST localhost:8080/api/applications \
  -H 'Content-Type: application/json' \
  -d '{"company":"Acme GmbH","position":"Backend Developer"}'

# move it through the pipeline
curl -X PATCH localhost:8080/api/applications/1/status \
  -H 'Content-Type: application/json' -d '{"status":"APPLIED"}'

# list, filtered and paginated
curl 'localhost:8080/api/applications?status=APPLIED&company=acme&page=0&size=20'
```

## API

| Method | Path                              | Description                                         |
|--------|-----------------------------------|-----------------------------------------------------|
| POST   | `/api/applications`               | Create an application                               |
| GET    | `/api/applications`               | List, filter by `status` / `company`, paginated     |
| GET    | `/api/applications/{id}`          | Get one, including its full status history          |
| PUT    | `/api/applications/{id}`          | Update descriptive fields                           |
| PATCH  | `/api/applications/{id}/status`   | Change status (invalid transitions return `409`)    |
| DELETE | `/api/applications/{id}`          | Delete                                              |

### Status lifecycle

```
WISHLIST ──> APPLIED ──> INTERVIEW ──> OFFER
    │           │            │           │
    └───────────┴────────────┴───────────┴──> WITHDRAWN
                │            │
                └────────────┴──> REJECTED
```

`REJECTED` and `WITHDRAWN` are terminal. Every change is stored in `status_change`.

## Design decisions

- **Transitions live in the domain, not the controller.** `ApplicationStatus` knows its allowed next states and
  `JobApplication.moveTo` enforces them, so every entry point shares the same rules and they are unit-testable
  without Spring.
- **Flyway owns the schema, Hibernate only validates it** (`ddl-auto: validate`). Schema changes are explicit,
  reviewable and repeatable.
- **Open-in-view is disabled.** Data needed for a response is loaded deliberately (entity graph for the detail
  view, a lighter summary for lists), which avoids hidden lazy loading and N+1 queries.
- **One error shape.** Errors are returned as RFC 7807 `ProblemDetail` responses: `404` not found, `409` invalid
  transition, `400` validation failure with per-field messages.
- **Integration tests use a real PostgreSQL** via Testcontainers instead of an in-memory database, so migrations,
  JPA mappings and SQL behave as in production.
- **Bounded input.** Page size is capped, strings have maximum lengths in both validation and schema.

## Development

Requirements: Java 21, Maven, Docker (for the integration tests).

```bash
mvn verify                     # unit + integration tests
docker compose up db -d        # only the database
mvn spring-boot:run            # run the app against it
```

## CI/CD

`.github/workflows/ci.yml` builds and runs all tests on every push and pull request.
On `main`, it additionally builds the Docker image and pushes it to GitHub Container Registry.

## Possible next steps

- Authentication (Spring Security with JWT) and per-user applications
- Reminders for applications without a status change for N days
- Metrics dashboard (Micrometer is already on the classpath through Actuator)
- Deployment manifests (Kubernetes / Helm)

## License

MIT
