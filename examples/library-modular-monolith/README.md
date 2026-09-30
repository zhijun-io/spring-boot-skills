# library-modular-monolith

This example uses the same library rental business as the layered and hexagonal examples, with enforced modular-monolith
boundaries.

## Selected shape

- Spring Boot `4.1.1`, Java `25`, Maven, runnable JAR
- `modular-monolith + hexagonal + feature + single-project`
- Business modules: `book` and `rental`
- Each module owns its domain, application, and adapters
- Cross-module access is limited to the public `book.api` contract
- PostgreSQL with MyBatis-Plus `3.5.15`, Flyway, and Spring Boot Docker Compose support
- REST/JSON, validation, Problem Details, OpenAPI, Actuator, Lombok, MapStruct
- PostgreSQL Testcontainers and ArchUnit module-boundary tests

## Run

Docker is required for PostgreSQL and the integration tests.

```bash
./mvnw test
./mvnw spring-boot:run
```

OpenAPI is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## API slice

- `POST /api/books`
- `GET /api/books/{bookId}`
- `POST /api/books/{bookId}/reviews` with `X-User-Id`
- `POST /api/rentals`
- `POST /api/rentals/{rentalId}/return`
