# Keep Additional Features Optional

RestTestClient, HTTP Service Clients, API version negotiation, JSpecify null-safety, security, observability, caching,
messaging, resilience, scheduling, batch, Spring Modulith, GraphQL, WebFlux, native-image builds, container packaging,
SDKMAN, and Taskfile remain opt-in. They solve distinct problems and each adds configuration, dependencies, or
development workflow policy that cannot be inferred from an ordinary REST project request.

The supported choices and their triggers are documented in `references/optional-features.md`. A selected feature must have
an explicit contract and tests; an unselected feature must not add dependencies or placeholder code.
