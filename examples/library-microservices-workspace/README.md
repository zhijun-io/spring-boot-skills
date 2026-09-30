# library-microservices-workspace

This workspace uses the same library rental business as the monolith examples, split into independently runnable
services.

## Selected shape

- Spring Boot `4.1.1`, Java `25`, Maven, runnable JAR per service
- `microservices + hexagonal + feature + independent-projects`
- `catalog-service` owns books, reviews, and inventory
- `rental-service` owns rentals and calls catalog inventory through an explicit HTTP port and adapter
- Each service owns its own database and Flyway migrations
- One PostgreSQL container is shared only as local infrastructure; the databases and tables are separate

The workspace deliberately has no root Maven reactor, gateway, discovery server, config server, broker, shared business
library, or service container. The services remain independently runnable and deployable.

## Run

Start the shared infrastructure once from the workspace root:

```bash
docker compose up -d postgres
```

Run the services in separate terminals:

```bash
cd catalog-service
./mvnw test
./mvnw spring-boot:run
```

```bash
cd rental-service
./mvnw test
./mvnw spring-boot:run
```

The catalog service listens on `8081`; the rental service listens on `8082`. The rental service uses
`CATALOG_SERVICE_BASE_URL` to configure the catalog endpoint and defaults to `http://localhost:8081`.

## HTTP contract

The rental service calls these catalog-owned internal endpoints:

- `POST /internal/books/{bookId}/reserve` returns `204`, `404 BOOK_NOT_FOUND`, or `409 BOOK_UNAVAILABLE`
- `POST /internal/books/{bookId}/release` returns `204` or `404 BOOK_NOT_FOUND`

The adapter maps connection and unexpected upstream failures to `503 CATALOG_UNAVAILABLE`; it does not share catalog
domain classes, mappers, repositories, or database tables.

OpenAPI is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html` on each service.
