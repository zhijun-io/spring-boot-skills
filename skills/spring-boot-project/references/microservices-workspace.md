# Microservices Workspace

Generate a microservices workspace only when the user names at least two services and defines each service's primary
responsibility. A microservices topology is a set of independently runnable and deployable applications, not a package
layout inside one Spring Boot application.

## Required Inputs

Collect these inputs before generating service code:

- service name and responsibility for every service;
- owned resources and persistence boundary for every service;
- synchronous HTTP or asynchronous messaging contracts, if services communicate;
- independent deployment, scaling, or failure-isolation requirements;
- selected `workspace_layout`: `independent-projects` or `maven-reactor`.

Ask about a missing item only when it changes generated code or infrastructure. Do not invent a gateway, service discovery,
configuration server, broker, shared business library, or cross-service database contract.

## Workspace Layouts

### Independent Projects

Generate one complete Maven Spring Boot project per service. Each service has its own `pom.xml`, application entry point,
configuration, package namespace, tests, and artifact. The workspace root contains only the requested orchestration,
documentation, and development tooling. Do not generate a root Maven reactor.

### Maven Reactor

Generate a root `pom.xml` with `pom` packaging and one module entry per service. Each service module still has its own
Spring Boot application, configuration, tests, and runnable artifact. Use the root only for aggregation and explicitly
requested dependency or plugin management. Do not introduce production dependencies between service modules.

## Service Boundaries

Each service owns its domain model, application logic, API boundary, persistence mappings, and database contract. Do not
share tables, MyBatis-Plus mappers, persistence models, or domain classes between services. Shared wire contracts are
allowed only when the user defines their ownership and versioning; keep them at an explicit contract boundary.

When persistence is enabled for multiple services, keep database ownership separate. The default local setup uses one
PostgreSQL container with one database per service; a shared PostgreSQL server is only a deployment convenience and
services must not share business tables or migrations.

The root `compose.yaml` starts only the infrastructure required by the selected services. Add service containers only
when container packaging is explicitly selected. Services run through their Maven Wrapper by default.

## Communication

Generate service-to-service HTTP clients or messaging only when the user defines the interaction contract. Put HTTP calls
behind outbound ports and adapters for Hexagonal services. Add timeout, error, retry, idempotency, and contract tests
according to the selected communication style. Do not add infrastructure components merely because the topology is
microservices.

## Validation

Run each service's tests and architecture tests. For `maven-reactor`, also run the root Maven test. If Compose or explicit
container workflows are requested, validate the Compose configuration and the selected service startup flow. Keep the
workspace when one service fails and report the first actionable service and command.
