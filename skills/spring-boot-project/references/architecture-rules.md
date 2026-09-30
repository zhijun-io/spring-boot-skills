# Architecture Rules

Generate one `ArchitectureTest` test class for the selected valid combination. Use `com.tngtech.archunit:archunit-junit5` in test scope and import only production classes under the generated base package. Keep rules specific to the selected combination; do not add broad package-name rules that make unrelated classes illegal.

## Shared Rules

- Domain classes do not depend on web, persistence, or adapter packages.
- API response types do not expose persistence models.
- Test classes and generated MapStruct implementation classes are outside the production boundary rules.
- Keep framework-specific rules focused on the selected boundary. ArchUnit verifies static dependencies; Spring context tests verify runtime wiring.

## Combinations

### Monolith + Layered + Technical

- Controllers may depend on services, mappers, and API DTOs.
- Services may depend on entities and persistence abstractions.
- Services and entities do not depend on controllers.
- Entities do not depend on web, validation, or persistence adapter packages.

### Monolith + Layered + Feature-Layered

- Each feature owns its controller, service, mapper, API, domain, and persistence packages.
- Cross-feature code uses a feature's declared application API or event types.
- A feature's controller does not depend directly on another feature's persistence or infrastructure package.
- Shared configuration and infrastructure do not contain business feature classes.

### Monolith + Hexagonal + Feature

- Domain packages do not depend on Spring, web, persistence, or adapter packages.
- Application packages may depend on domain types and explicit inbound or outbound ports.
- Inbound and outbound adapters depend on application ports; the application core does not depend on adapters.
- API DTOs and persistence models remain in their adapters and are mapped at the boundary.

### Modular Monolith + Hexagonal + Feature

- Apply the Hexagonal rules inside every named business module.
- A module may expose only its declared `api` package or event types to other modules.
- A module must not depend on another module's `domain`, `application`, `adapter`, or infrastructure package.
- Require at least two user-named business modules; never create a technical placeholder module.

### Microservices + Layered + Technical

- Apply the technical layered rules independently inside every service.
- A service must not import another service's production packages, persistence models, or repositories.
- Each service has its own application entry point, configuration, test boundary, and independently buildable artifact.
- Service-to-service calls use an explicitly defined HTTP or messaging contract at the adapter boundary.

### Microservices + Hexagonal + Feature

- Apply the Hexagonal rules independently inside every service.
- A service must not import another service's domain, application, adapter, or infrastructure packages.
- Each service owns its domain and persistence boundary; shared business persistence is prohibited.
- Service-to-service calls use explicit outbound ports and adapters, with contract tests when a contract is defined.

### Microservices Workspace Layouts

- `independent-projects` has one `pom.xml` per service and no root Maven reactor. Root files may contain documentation,
  Compose configuration, and selected Taskfile tasks.
- `maven-reactor` has a root `pom.xml` with `pom` packaging and one module per service. The root may centralize versions
  and plugins, but services must remain independently runnable and deployable.
- Generate one architecture test class per service. A root build test must not replace service-level boundary tests.

## Validation

Run the architecture tests with the ordinary Maven test command for a single project, or in every service for a
microservices workspace. Keep the generated project when a rule fails and report the first violated rule and dependency
path. Do not weaken or delete a rule to make a generated project pass; correct the package placement or dependency
direction.
