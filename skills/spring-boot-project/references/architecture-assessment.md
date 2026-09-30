# Architecture Assessment

Use this reference only when the user asks for an architecture recommendation or describes a non-trivial domain without
choosing an architecture. The assessment recommends one value on each architecture axis and, for microservices, one
workspace layout; it does not create a new architecture category. Do not turn a simple technical starter into an interview.

## Trigger Rules

- Explicit architecture selection: skip assessment and validate the requested values against the matrix.
- Architecture recommendation requested: assess the unanswered decisions below and recommend a supported combination.
- Meaningful domain with no architecture selection: assess only the decisions that affect package boundaries or dependency
  direction.
- Technical-only starter with no business domain: use `monolith + layered + feature-layered`, without architecture
  questions.
- Microservices request: require at least two named services, their responsibilities, and a selected or recommended
  `workspace_layout`. Do not silently convert it to a modular monolith or generate one service and label the result as
  microservices.

## Decisions

Ask only for facts that are missing and materially change the result:

1. **Business complexity:** Is the system mostly CRUD, or does it have important invariants, workflows, and
   cross-resource transactions?
2. **Business boundaries:** How many named business modules or feature areas need independent ownership and boundaries?
3. **Deployment topology:** Is there one deployable application, or is module separation required inside that one
   deployable unit?
4. **Dependency isolation:** Must the domain and application core remain independent of Spring, web, and persistence
   technologies, or is conventional layer coupling acceptable?
5. **Package preference:** Has the user requested technical packages, feature packages, or feature-local layers?
6. **Workspace layout:** For microservices, should the workspace use independent Maven projects or a Maven reactor? Use
   `independent-projects` when no build coupling is required; choose `maven-reactor` when unified dependency management
   and repository-wide builds are explicit requirements.

Do not ask about team size, project lifespan, or future microservice extraction unless the user explicitly says one of them
is an architectural constraint. They are not inputs to the first-version matrix.

## Recommendation Rules

Choose `topology` as follows:

- `monolith` for one deployable application without enforced independent business modules.
- `modular-monolith` for one deployable application with at least two user-named business modules and enforced module
  boundaries. Never invent a technical module to satisfy the count.
- `microservices` for at least two independently deployable services with explicit service responsibilities and service
  boundaries. Use it only when independent deployment, scaling, or failure isolation is a stated requirement.

Choose `dependency_style` as follows:

- `layered` for CRUD-oriented applications where ordinary application, API, and persistence layering is sufficient.
- `hexagonal` when domain rules are significant or the user requires ports, adapters, framework independence, or
  replaceable infrastructure.

Choose `package_layout` as follows:

- `technical` for a small conventional application where top-level technical layers improve navigation.
- `feature-layered` for a monolith with multiple business features that each need local technical layers; this is the
  default layout for an ordinary multi-feature application.
- `feature` for hexagonal features or modular business boundaries where each feature owns its internal code.

Choose `workspace_layout` as follows:

- `single-project` for monoliths and modular monoliths.
- `independent-projects` for microservices by default, when each service should have an independent Maven lifecycle.
- `maven-reactor` for microservices only when the user wants a root Maven build and shared dependency management.

Only recommend combinations listed in [architecture-matrix.md](architecture-matrix.md). In particular, do not combine
`modular-monolith` with `technical`, or `hexagonal` with `technical` or `feature-layered` in the first version.

## Output

Before generation, state the selected architecture combination and workspace layout, and explain why each fits the stated
requirements. Record the same choices in the generated README and enforce the architecture with the matching ArchUnit
tests. If two supported combinations remain materially different and the requirements do not decide between them, ask one
focused question instead of guessing.
