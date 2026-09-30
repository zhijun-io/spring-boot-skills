# library-layered-monolith

This example was created with the `spring-boot-project` skill.

## Selected shape

- Spring Boot `4.1.1`, Java `25`, Maven, runnable JAR
- `monolith + layered + feature-layered + single-project`
- Business features: `book` and `rental`
- PostgreSQL with MyBatis-Plus `3.5.15`
- Flyway migrations and Spring Boot Docker Compose support
- REST/JSON, validation, Problem Details, OpenAPI, Actuator, Lombok, MapStruct
- Testcontainers PostgreSQL and ArchUnit boundary tests

## Run

Docker is required for PostgreSQL and the integration test.

```bash
./mvnw test
./mvnw spring-boot:run
```

The application uses `compose.yaml` to start PostgreSQL during local development. OpenAPI is available at
`/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## API slice

- `POST /api/books`
- `GET /api/books/{bookId}`
- `POST /api/books/{bookId}/reviews` with `X-User-Id`
- `POST /api/rentals`
- `POST /api/rentals/{rentalId}/return`
