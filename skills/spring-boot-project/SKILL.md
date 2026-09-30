---
name: spring-boot-project
description: Create new Spring Boot 4.x Java/Maven applications or microservices workspaces from natural-language requirements using Spring Initializr, validated architecture combinations, Actuator, OpenAPI, and generated tests; add MyBatis-Plus/PostgreSQL persistence when required. Use for new projects; do not use to modify existing applications.
---

# Spring Boot Project

Create a new, runnable Spring Boot project or microservices workspace. Keep project creation separate from modifying an
existing application.

## When to Use

Use this skill when the user wants a new Spring Boot application, service, API project, or microservices workspace
generated from requirements, including a selected architecture, business resources, persistence, tests, or optional
integrations. Use it when the target directory is new or empty and the expected outcome is a runnable Maven project or
an independently runnable multi-service workspace.

When the request does not define enough business detail to generate a meaningful domain, ask for resources, fields,
relationships, constraints, and operations before creating domain code. A bare request for a Spring Boot starter may
create only the configured technical baseline and tests.

Use ordinary project-editing, debugging, migration, or review workflows for an existing Spring Boot project. Use a
separate explanation workflow when the user only wants Spring Boot concepts or documentation and no project artifact.

## Architecture Assessment

Use a lightweight assessment only when the user asks for an architecture recommendation or gives a meaningful domain
without selecting an architecture. If the user explicitly selects an architecture, validate that selection directly
against the matrix. For a technical-only starter with no business domain, use the matrix default without asking
architecture questions. Read [references/architecture-assessment.md](references/architecture-assessment.md) only for the
recommendation branch.

## Required Boundaries

- The target directory must not exist or must be empty. Refuse non-empty targets; never overwrite existing project
  files. A microservices workspace is one target containing multiple newly generated service projects.
- Default to the latest compatible Spring Boot `4.x` version reported by current Spring Initializr metadata, Java `25`,
  Maven, and a runnable JAR. Support Java `17` and newer when the user selects a compatible version; recommend Java `25`
  for new projects. Allow an explicit Spring Boot `4.x` version only after verifying it with current official metadata.
  A narrowly scoped exception permits a clearly named Spring Boot `3.x` migration fixture when the user explicitly needs
  an acceptance source for `spring-boot-migration`: verify the released parent and dependency BOM from the official Maven
  repository, use no snapshot, and document that the result is a test fixture rather than the recommended new-project
  baseline. Do not generalize this exception to ordinary new applications.
- Use Spring Initializr as the scaffold source. Probe the local CLI with `command -v spring` followed by
  `spring help init`; use `spring init` only when that probe succeeds. Prefer it with explicit `--build maven`,
  `--boot-version`, `--java-version`, `--language java`, `--packaging jar`, `--dependencies`, and `--extract` options.
  For the CLI, set `--target https://start.spring.io?configurationFileFormat=yaml`; for a direct HTTP request, append the
  same query parameter. Initializr then creates `application.yaml` directly. A broken package-manager shim is unavailable
  until its managed version is activated; in that case call the official Initializr service directly with the same
  parameter. Never use `--force` against a target. If Initializr or required dependencies cannot be reached or resolved,
  report the failure and stop. Do not fall back to a vendored full template, an unverified mirror, H2, or an older Spring
  Boot line.
- Before constructing any Initializr request, verify that the selected Spring Boot and Java versions are listed as
  compatible by current Initializr metadata. Java `17` is the minimum; reject unsupported Java versions instead of
  silently lowering or raising the requested version.
- Default `groupId` to `com.example`, derive the artifact from the target directory, and derive the package as `groupId`
  plus a valid lowercase artifact segment by replacing separators and hyphens with dots and removing other invalid
  characters. Use `application.yaml`; ask when the derived package would be ambiguous or empty.
- Enable Problem Details explicitly with `spring.mvc.problemdetails.enabled=true` and document `/v3/api-docs` and the
  Swagger UI endpoint in the generated README.
- Generate a short project or workspace-root `README.md` with the selected parameters, architecture, test, and OpenAPI
  commands; include PostgreSQL startup only when persistence is enabled. Do not write a generic Spring Boot tutorial.

## Workflow

1. Inspect the request and target path. Extract the project or workspace name, service names and boundaries when
   applicable, group, package, resources, fields, relationships, operations, architecture axes, optional integrations,
   selected developer tooling, and whether the target is an explicitly requested migration fixture.
2. Ask only for missing information that materially changes the generated project. If the request is clear, generate
   directly. If the domain requirement is absent, ask for the domain, resources, fields, relationships, and operations;
   do not invent a business example or generate default business entities.
3. Resolve architecture using [references/architecture-matrix.md](references/architecture-matrix.md). If the user asks
   for a recommendation or leaves architecture unspecified for a non-trivial domain, first follow
   [references/architecture-assessment.md](references/architecture-assessment.md). The architecture axes are
   `topology`, `dependency_style`, and `package_layout`. For `microservices`, choose `workspace_layout` separately and
   read
   [references/microservices-workspace.md](references/microservices-workspace.md) and require its service-boundary
   inputs. Reject unsupported combinations instead of silently replacing them. For a supported combination, read
   [references/architecture-rules.md](references/architecture-rules.md) and generate its matching ArchUnit rules.
4. Check the current Initializr metadata before constructing `--dependencies`; use its dependency IDs (for example,
   Spring MVC is `web`, not `webmvc`). Generate the project with `configurationFileFormat=yaml` and verify that
   `src/main/resources/application.yaml` exists before adding configuration. For a microservices workspace, perform this
  check independently for every service. If a compatible Initializr implementation
   still emits `application.properties`, convert it immediately and verify that the generated project has one canonical
   application configuration format. Use the official metadata or dependency documentation to resolve compatible
   coordinates and versions. Verify that the generated parent POM and dependency coordinates resolve from the configured
   repositories; normalize an Initializr display label such as a release suffix only when the corresponding official Maven
   coordinate is confirmed. For the explicit migration-fixture exception, current Initializr metadata may no longer list
   the requested Boot 3.x line; verify the exact released parent and dependency BOM from the official Maven repository,
   then adapt only the fixture's build coordinates and legacy migration surface. After resolution, verify every non-trivial imported API against the resolved artifacts (for
   example with `mvn dependency:tree`, `jar tf`, `javap`, or compilation of the generated tests). Do not infer a package,
   class name, generic signature, annotation, or test-slice package from an older Spring Boot or Testcontainers example.
   Verify that the selected MyBatis-Plus starter supports the chosen Spring Boot line; if no compatible coordinate is
   available, stop and report it.
5. Generate explicit domain, application, API, persistence, migration, and test code from the request. For every
   user-declared operation, define its success result, validation failures, not-found or conflict behavior, and state
   transitions where applicable. If an operation changes multiple records or manages finite capacity, define its
   transaction boundary and concurrency policy before writing persistence code. For `microservices`, generate each
   service as an independently runnable application and generate only the root workspace files required by the selected
   `workspace_layout`. Do not generate domain code until the business requirements are concrete enough to define its
   contract. When the same business requirements are rendered in different supported architectures, preserve the same
   operation set, success/failure semantics, state transitions, and persistence invariants; architecture changes
   dependency direction, not product behavior. When selected, generate the project-level or workspace-level
   developer-tooling files described in
   [references/optional-features.md](references/optional-features.md).
6. Validate the result before handing it off. Preserve the generated project on failure and report the first actionable
   root cause.

For each user-defined resource, capture its name, field names and types, requiredness, validation constraints,
relationships, and requested operations. Ask for missing field semantics rather than inferring a database contract when
the omission is material.

## Stack Layers

Use the first layer for every project or service, the second layer when persistence is requested, and the third layer only
when the request selects an optional capability.

### Core Default

- Spring MVC REST/JSON, Bean Validation, Spring Problem Details, `springdoc-openapi-starter-webmvc-ui`, and
  `spring-boot-starter-actuator`. Keep Actuator's default endpoint exposure unless the request defines an explicit
  operational requirement; do not expose every endpoint by default.
- Generate centralized exception handling with `@RestControllerAdvice`. Map validation, malformed request bodies or
  parameters, declared domain/application failures, and unexpected failures to Problem Details. Every client-visible
  error must have a stable `code`; unexpected failures use a generic internal-error response and keep diagnostic details
  in server logs only. In feature-oriented layouts, shared advice may handle a generic application-exception contract
  carrying status, stable code, and message key, but must not import concrete feature exception classes. Use feature-local
  advice instead when a feature needs behavior beyond that contract. A broad fallback handler such as
  `@ExceptionHandler(Exception.class)` must not preempt declared application failures or Spring's malformed-request
  handling: give the custom resolver an explicit precedence strategy (or use a narrower handler) and verify the actual
  resolver ordering against the selected Spring Boot version. Verify exception types against the selected Spring Boot
  version rather than copying an older package name.
- Enable message resolution with Spring `MessageSource`, a `messages.properties` fallback bundle, and
  `Accept-Language` locale resolution. Localize validation and problem-detail messages; every user-visible validation or
  error message must use a message key with a fallback entry in `messages.properties`, never a literal annotation string
  or hardcoded fallback. Add locale-specific bundles when the request defines supported locales instead of inventing
  business translations.
- Include `spring-boot-testcontainers` and `org.testcontainers:testcontainers-junit-jupiter` in every generated project
  as the default integration-test technology. Add container modules only for infrastructure the project actually uses;
  when PostgreSQL persistence is required, use `org.testcontainers:testcontainers-postgresql`. For Spring Boot 4.x,
  use the Testcontainers dependency versions managed by Spring Boot; do not add the old
  `org.testcontainers:postgresql` coordinate or pin a separate Testcontainers version unless an explicit compatibility
  override is requested. After dependency resolution, inspect the selected Testcontainers JARs and compile a minimal
  container declaration to confirm the actual package, class name, constructors, generic signature, and lifecycle style.
  Choose either Spring-managed `@Bean` plus `@ServiceConnection` or JUnit-managed `@Container` plus `@Testcontainers` for
  a given test context; do not combine both lifecycle mechanisms accidentally. Docker is required for container-backed
  tests; do not substitute H2 when Docker is unavailable.
- Include `com.tngtech.archunit:archunit-junit5` in test scope in every generated project. Resolve a current compatible
  ArchUnit version instead of pinning a historical version. Keep ArchUnit out of runtime dependencies and generate
  architecture tests from the selected supported combination; verify the dependency against the selected Spring Boot
  test stack.
- Lombok and MapStruct with correctly configured annotation processors, including the Lombok-MapStruct binding when
  required by the selected versions. Restrict Lombok to local accessors, builders, and constructors; do not use `@Data`.
  MapStruct may map API DTOs, application commands/results, and persistence objects, but persistence objects must not be
  returned directly from APIs. In layered projects, keep web/API mapping separate from domain/entity persistence
  mapping; in hexagonal projects, keep DTOs and persistence models inside their respective adapters.

### Persistence Default

- When persistence is required, use MyBatis-Plus with PostgreSQL and explicit Flyway SQL migrations. For the selected
  Spring Boot `4.x` version, use the Initializr-managed `spring-boot-starter-flyway` when available and verify
  PostgreSQL migration support. Do not use ORM auto-DDL or MyBatis-Plus table creation as the schema contract.
- When persistence is required, generate PostgreSQL in `compose.yaml` and include Spring Boot's
  `spring-boot-docker-compose` runtime support. Compose starts the database only; the application runs through Maven and
  lets Spring Boot manage the Compose lifecycle and service connection. For a microservices workspace, use the single
  root Compose file and follow [references/microservices-workspace.md](references/microservices-workspace.md); do not
  generate one database Compose file per service.

### Optional Stack

Read [references/optional-features.md](references/optional-features.md) when the request selects an optional capability.
Do not add optional dependencies or placeholder code unless their behavior and boundaries are requested. Spring Modulith is
the exception for the selected `modular-monolith` topology: it is required there, not optional. Add its compatible core
starter, declare the application with `@Modulithic`, expose cross-module APIs as named interfaces, and run
`ApplicationModules.of(...).verify()` in the test suite. A modular monolith must not be represented only by feature
packages and custom ArchUnit rules.

### Optional Developer Tooling

SDKMAN and Taskfile are project-development conveniences, not application dependencies. Generate them only when the
request selects them. SDKMAN requires an explicit candidate/version mapping before creating `.sdkmanrc`; it must not
change the user's global SDKMAN configuration. Taskfile may wrap the generated Maven Wrapper and Docker/Compose
commands, but only for files and workflows that the project actually contains or the request explicitly defines. Do
not duplicate build or container configuration in Taskfile. Document selected tools and their commands in the project or
workspace README.

### Microservices Workspace

Use [references/microservices-workspace.md](references/microservices-workspace.md) for service boundaries, workspace
layout, inter-service communication, and service-level validation. Do not add a gateway, discovery server, config server,
message broker, or shared business library unless the request defines its contract.

## Persistence Rules

- Only generate persistence artifacts when the request requires persistence. When persistence is enabled, generate SQL
  migrations from the declared resources, fields, relationships, and constraints. Make identifiers, foreign keys,
  amounts, timestamps, and indexes explicit.
- Keep persistence models, application models, and API DTOs separate when the selected architecture requires those
  boundaries.
- Use one transaction for multi-record commands. Do not infer external side effects from relationships or resource
  names.
- When a declared operation changes a finite quantity, quota, balance, or availability, make the invariant explicit and
  enforce it atomically. Prefer a conditional update or an appropriate row-locking strategy, check the affected-row
  count, and map exhausted capacity or invalid state transitions to a stable application error. For state transitions,
  define idempotency and uniqueness rules rather than relying on an in-memory check followed by an unconditional write.
  A read-check-write transition must be replaced by a conditional state update or row lock when concurrent requests can
  affect the same record.

## Validation

- Run any configured formatting/check goal, then run Maven compilation and tests. The normal minimum is `mvn test`; run
  `mvn package` when packaging behavior is part of the request.
- Tests should cover application/service behavior and HTTP/API behavior. Use unit tests for pure service logic, MockMvc
  or an equivalent in-process client for controller behavior, and Testcontainers only where a real external
  infrastructure dependency is under test. When persistence is enabled, cover MyBatis-Plus mappings and API flows
  against PostgreSQL Testcontainers. Any full-context `@SpringBootTest` that loads Flyway or MyBatis must bind a
  PostgreSQL Testcontainer through either a Spring-managed `@Bean` with `@ServiceConnection` or a JUnit-managed
  `@Container` with `@ServiceConnection`; never make it depend on a developer's local PostgreSQL instance.
  For Spring Boot 4 MVC tests, use `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
  Build an operation test matrix: every declared operation needs at least one success test and one relevant failure or
  boundary test; stateful or capacity-limited operations also need conflict, idempotency, and concurrency coverage when
  those behaviors are part of the requirement. When comparing architecture variants for the same requirements, run the
  same operation matrix against each variant. At the API boundary, test malformed JSON and parameters, validation,
  declared not-found/conflict failures, and at least one unexpected failure path when the application exposes a safe
  trigger. Assert status, `application/problem+json` response shape, stable `code`, and localized `detail`; do not only
  assert that an exception was thrown. Specifically verify that malformed requests and declared application failures
  are not converted into the generic internal-error response by a broad advice handler.
  In a microservices workspace, run the service tests independently and run the root Maven reactor test when
  `maven-reactor` is selected. Run the generated ArchUnit boundary tests as part of `mvn test`; a boundary violation is
  a validation failure. Do not add performance tests unless requested.
- If Docker, Maven, dependency resolution, or tests fail, keep the generated files, stop at the first root cause, and
  state the missing prerequisite or failing command. Do not repeatedly rewrite the project.
