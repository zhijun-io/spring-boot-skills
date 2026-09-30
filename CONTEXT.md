# Spring Boot Project Skill Context

This context defines the terms used by the skill that creates Spring Boot projects and microservices workspaces.

## Project Scope

**Generation Mode**:
The output shape: `single-project` for one Spring Boot application or `microservices-workspace` for multiple
independently runnable services.

**Spring Boot 4 Project**:
A Java 17+ Maven application using Spring Boot 4 and packaged as a runnable JAR by default. Java 25 is the recommended
version for new projects.

**Microservices Workspace**:
A workspace containing at least two independently runnable and deployable Spring Boot services with explicit service
responsibilities and boundaries. It is not a package layout inside one application.

**Service Boundary**:
The ownership boundary around a service's domain, application behavior, API, persistence mappings, and database contract.
Services do not share business tables, migrations, persistence models, or domain classes.

## Architecture Language

**Architecture Selection**:
Three independent decisions: `topology`, `dependency_style`, and `package_layout`. Only verified combinations are
supported.

**Topology**:
The deployment and module boundary shape: `monolith`, `modular-monolith`, or `microservices`.

**Dependency Style**:
The allowed dependency direction: `layered` or `hexagonal`. Onion is reserved until it has a separately verified
generation strategy.

**Package Layout**:
The namespace arrangement: `technical`, `feature`, or `feature-layered`.

**Workspace Layout**:
The repository and build organization: `single-project`, `independent-projects`, or `maven-reactor`. It is separate from
the architecture axes and is required only to distinguish microservices workspace organization.

**Independent Projects**:
A microservices workspace with one Maven project per service and no root Maven reactor. The root may contain documentation,
infrastructure orchestration, and selected developer tooling.

**Maven Reactor**:
A microservices workspace with a root `pom.xml` that aggregates service modules and manages explicitly shared build
versions or plugins. Each service remains independently runnable and deployable; service modules do not share business
code.

**Default Architecture Selection**:
`monolith` topology with `layered` dependency style, `feature-layered` package layout, and `single-project` workspace
layout.

## Technical Baselines

**Project Creation Skill**:
A reusable skill that creates new applications or microservices workspaces from declared defaults and user-provided
requirements, preferring the local Spring Boot CLI when available.

**Business Template**:
A runnable application or service baseline that includes a web API and business-service boundary, adding a persistence
boundary only when the project or service requires persistence.

**Persistence Baseline**:
When persistence is required, use MyBatis-Plus backed by PostgreSQL, with Flyway migrations and Spring Boot Docker
Compose support. In a microservices workspace, each service owns its database contract and migrations.

**Operational Baseline**:
Spring Boot Actuator is included in every generated application or service; its default endpoint exposure is retained
unless the user requests explicit operational configuration.

**Core Default Stack**:
Spring MVC REST/JSON, Bean Validation, Spring Problem Details, centralized exception handling, message resolution,
OpenAPI, Actuator, Lombok, MapStruct, Testcontainers support, and ArchUnit.

**Persistence Default Stack**:
When persistence is required, use MyBatis-Plus, PostgreSQL, Flyway, Spring Boot Docker Compose support, and PostgreSQL
Testcontainers.

**Testing Baseline**:
Spring Boot Testcontainers integration, Testcontainers JUnit Jupiter support, and ArchUnit JUnit 5 support are included
in every generated application or service. Architecture tests are generated for the selected combination and run in the
normal Maven test phase.

**API Baseline**:
A REST/JSON API with Bean Validation, Spring Problem Details, centralized exception handling, message resolution,
`springdoc-openapi-starter-webmvc-ui`, generated OpenAPI documentation, and unversioned resource paths such as
`/api/{resource}`. API version negotiation is optional.

**API Version Policy**:
API version negotiation is disabled by default. When selected, the supported preset uses the `X-Version` request header,
default `1.0.0`, and Spring MVC controller mappings.

**Exception Handling Baseline**:
Generated applications use `@RestControllerAdvice` to map validation, malformed request, declared application, and
unexpected failures to Problem Details without exposing internal details. Every client-visible error has a stable code;
unexpected failures use a generic internal-error response. A broad fallback handler must not preempt declared failures or
malformed-request handling; custom resolver precedence is explicit and verified with API tests.

**Internationalization Baseline**:
Generated applications use Spring `MessageSource`, a fallback `messages.properties` bundle, and `Accept-Language`
locale resolution for validation and problem-detail messages. User-visible messages use message keys rather than literal
annotation strings. Locale-specific bundles are added when requested.

**Optional Stack**:
RestTestClient, HTTP Service Clients, API version negotiation, JSpecify null-safety, security, observability, caching,
messaging, resilience, scheduling, batch, Spring Modulith, GraphQL, WebFlux, native-image builds, container packaging,
SDKMAN, and Taskfile are opt-in capabilities.

## Generation Policy

**Resource Generation Policy**:
Generate only the user's declared business resources, fields, and constraints. When business requirements are missing or
materially ambiguous, ask for them instead of inventing a default domain example.

**Generated Project Validation**:
After generation, run formatting checks, compilation, and tests for every project or service. For a Maven reactor, also
run the root build. Every declared operation needs success and relevant failure or boundary tests; stateful or
capacity-limited operations also need conflict and concurrency tests when required. Preserve generated files and stop on
the first root cause when validation fails. Resolve dependencies before validating imports and inspect version-sensitive
classes, packages, signatures, and test annotations against the resolved artifacts. API tests assert Problem Details
content type, stable error codes, and message resolution, including malformed requests and declared failures.