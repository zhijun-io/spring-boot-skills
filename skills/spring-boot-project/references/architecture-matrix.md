# Architecture Matrix

Architecture is selected on three separate axes. `workspace_layout` is a separate repository and build decision for
microservices. Each value on each axis is mutually exclusive, and only the combinations below are supported in the first
version. Every generated result must pass the service/workspace validation rules before it is handed off.

## Axes

### `topology`

- `monolith`: one application and one deployable unit.
- `modular-monolith`: one deployable unit with at least two named business modules and enforced module boundaries.
- `microservices`: two or more independently runnable and deployable services with explicit service boundaries.

### `dependency_style`

- `layered`: presentation depends on application/service code, which depends on persistence and infrastructure layers according to the generated contract.
- `hexagonal`: the application core depends on explicit ports; inbound and outbound adapters depend on those ports, while the domain remains framework-independent.

Onion is intentionally not a first-version value. Do not approximate it by renaming a Hexagonal directory.

### `package_layout`

- `technical`: top-level packages are technical layers such as `controller`, `service`, `mapper`, and `entity`.
- `feature`: top-level packages are business features; each feature owns its internal code.
- `feature-layered`: each business feature contains its technical layers, while cross-feature configuration and infrastructure remain separate.

### `workspace_layout`

- `single-project`: one Maven project and one application artifact. Valid for `monolith` and `modular-monolith`.
- `independent-projects`: a workspace containing one Maven project per microservice; the root contains orchestration and
  documentation, not a Maven reactor. This is the default microservices layout.
- `maven-reactor`: a root `pom.xml` aggregates one Maven module per microservice. Each module remains independently
  runnable and deployable, while the repository shares build and dependency management.

## Valid Combinations

| Topology | Dependency style | Package layout | Workspace layout | Use |
| --- | --- | --- | --- | --- |
| `monolith` | `layered` | `technical` | `single-project` | Small conventional application |
| `monolith` | `layered` | `feature-layered` | `single-project` | Default application shape |
| `monolith` | `hexagonal` | `feature` | `single-project` | Domain/application boundary per feature |
| `modular-monolith` | `hexagonal` | `feature` | `single-project` | Multiple business modules with ports and adapters |
| `microservices` | `layered` | `technical` | `independent-projects` | Independent simple services without a root reactor |
| `microservices` | `layered` | `technical` | `maven-reactor` | Independent simple services with a unified root build |
| `microservices` | `hexagonal` | `feature` | `independent-projects` | Independent services with explicit ports and adapters |
| `microservices` | `hexagonal` | `feature` | `maven-reactor` | Port-and-adapter services with a unified root build |

For `monolith` and `modular-monolith`, use `single-project`. For `microservices`, the dependency style and package layout
apply inside each service, while the selected workspace layout controls repository and build organization.

Reject every other combination in the first version. Do not silently coerce a requested value.

## Invariants

- `feature-layered` is a package layout, not a separate dependency style.
- `modular-monolith` requires at least two business module names from the user. Do not create a technical fake module to satisfy the count.
- `microservices` requires at least two user-named services and an explicit responsibility for each. Do not create
  technical placeholder services.
- Microservices must not share business tables or business production package dependencies. A root Maven reactor may
  manage versions. A contract-only module is the sole allowed shared production dependency and may contain only wire
  contracts or schemas, never domain, persistence, or business logic.
- `independent-projects` has no root Maven reactor. `maven-reactor` has a root `pom.xml` with `pom` packaging and one
  module entry per service.
- Each modular module owns its internal classes. Other modules may use only its declared public API or events.
- A `modular-monolith` uses Spring Modulith by default and must declare and verify its application modules. Spring Modulith
  remains optional for ordinary `monolith` projects.
- A persistence adapter must not leak into a domain or API package where the selected dependency style forbids it.
- The default is `monolith + layered + feature-layered + single-project` when no architecture is specified.
