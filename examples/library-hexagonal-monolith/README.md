# library-hexagonal-monolith

This example uses the same library rental business as `library-layered-monolith` with a different architecture.

## Selected shape

- Spring Boot `4.1.1`, Java `25`, Maven, runnable JAR
- `monolith + hexagonal + feature + single-project`
- Business features: `book` and `rental`
- Application ports under `book/application` and `rental/application`
- Web and PostgreSQL adapters under each feature's `adapter/in` and `adapter/out`
- PostgreSQL with MyBatis-Plus, Flyway, Spring Boot Docker Compose support, Testcontainers, and ArchUnit

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
