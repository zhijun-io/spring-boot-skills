# Optional Features

These capabilities are opt-in. Select them only when the request defines the behavior they support. Verify dependency
coordinates and APIs against the selected Spring Boot, Spring Framework, and library versions before generation.

## RestTestClient

Use `RestTestClient` when the request explicitly wants HTTP-level tests and its client API is useful for the test
boundary. Keep MockMvc or an equivalent in-process client as the default for ordinary MVC controller tests.
RestTestClient is a test tool and must remain in test scope.

Source: [Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing-spring-boot-applications-testing-with-resttestclient)

## HTTP Service Clients

Use Spring HTTP Service Clients when the application calls an external HTTP service and the user has defined its
contract, timeout, error, and retry behavior. Keep the client interface in an outbound application port or integration
boundary; do not add a client for an unspecified external system. Configure the underlying client and map remote
failures at the adapter boundary.

Source: [Calling REST Services](https://docs.spring.io/spring-boot/reference/io/rest-client.html)

## API Versioning

API version negotiation is disabled by default. When requested, keep resource URLs unversioned and select one explicit
strategy. The supported preset uses Spring MVC's `ApiVersionConfigurer` with `useRequestHeader("X-Version")`,
`setDefaultVersion("1.0.0")`, and `addSupportedVersions("1.0.0")`; controller mappings declare the version. Add tests
for omitted, supported, and unsupported versions.

Source: [Mapping Requests](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html)

## JSpecify Null-Safety

Use JSpecify annotations when the request requires an explicit nullness contract across module or library boundaries.
Add `org.jspecify:jspecify` with a version verified for the selected platform, annotate public contracts, and configure
the selected static analysis tool to consume the annotations. Do not annotate every local variable by default.

Source: [Null-safety](https://docs.spring.io/spring-framework/reference/core/null-safety.html)

## Security

Use Spring Security, OAuth2 Resource Server, or an OIDC client only when the authentication, authorization, token, and
tenant-isolation contract is defined. Keep security configuration at the application boundary and test anonymous,
authenticated, and forbidden requests.

Source: [Spring Security](https://docs.spring.io/spring-security/reference/)

## Observability

Use Micrometer registries, tracing, OpenTelemetry, or structured logging when the target monitoring and tracing systems
are defined. Add only the exporter or bridge required by that system; Actuator remains the core operational baseline.

Source: [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html)

## Cache and Messaging

Use Spring Cache with Redis or another provider only when cache ownership, expiration, invalidation, and stale-data policy
are defined. Use Kafka, RabbitMQ, or another broker only when message delivery, idempotency, ordering, and failure handling
are defined.

Sources: [Caching](https://docs.spring.io/spring-boot/reference/io/caching.html), [Messaging](https://docs.spring.io/spring-boot/reference/messaging/index.html)

## Resilience Features

Use Spring Framework or a selected resilience library only when the request defines a failure policy for external calls or resource
contention. Choose bounded retries, timeouts, concurrency limits, or rate limits deliberately; define what is safe to
retry and how failures surface. Do not add Resilience4j or another resilience library unless the request selects it
explicitly.

Source: [Resilience Features](https://docs.spring.io/spring-framework/reference/core/resilience.html)

## Developer Tooling

SDKMAN and Taskfile are opt-in project-development tooling. They do not belong in the default application stack and do
not justify runtime dependencies.

### SDKMAN

Generate `.sdkmanrc` only when the request selects SDKMAN and provides exact candidate/version mappings, such as the Java
distribution and version. Keep the file limited to the selected candidates. Align the Java candidate with the project's
configured Java version, but do not invent a vendor or patch version when the request leaves it unspecified. Never modify
the user's global SDKMAN configuration; explain that `sdk env` activates the project file. Document the selected mappings
and activation command in the project or workspace README.

### Taskfile

Generate `Taskfile.yml` only when the request selects Taskfile. Use the generated Maven Wrapper as the single build
implementation and provide only useful tasks that map to generated behavior, normally `test`, `package`, and `run`.
Taskfile may also wrap `docker` and `docker compose` commands. Add container tasks only when the project contains the
corresponding `Dockerfile` or `compose.yaml`, or when the request explicitly defines a Docker workflow. Typical tasks
are `image-build`, `compose-up`, `compose-down`, and `compose-logs`; select only those that map to an actual requested
operation. Do not copy container configuration into Taskfile, and do not add Compose lifecycle tasks merely because
Spring Boot Docker Compose support is present. Keep task commands aligned with the project's supported operating
systems; if a cross-platform Taskfile is required, resolve the wrapper command for each selected platform instead of
assuming a Unix shell. Document the available tasks in the project or workspace README.

## Other Optional Capabilities

Scheduling, Spring Batch, Spring Modulith, GraphQL, WebFlux, native-image builds, and container packaging are also opt-in.
Select them only when the request defines their runtime or delivery behavior.

## Validation

Optional application features must have behavior tests. Keep feature-specific dependencies and developer-tooling files
out of projects that do not select the feature, and report any version incompatibility instead of silently substituting
another library. When Taskfile is selected and the `task` executable is available, run `task --list` or the equivalent
syntax check; when SDKMAN is selected, validate the `.sdkmanrc` mappings against the requested project tool versions.
