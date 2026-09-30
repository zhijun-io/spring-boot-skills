# Use Validated Architecture Presets

The project creation skill models architecture across three separate axes: `topology`, `dependency_style`, and
`package_layout`. Repository and build organization is recorded separately as `workspace_layout`. Each value has
mutually exclusive options, but the skill accepts only combinations that are supported by the generation contract. This
keeps generated projects testable and coherent while representing the fact that Modular Monolith, Hexagonal, Onion, and
package organization describe different concerns.

The first version supports `monolith`, `modular-monolith`, and `microservices` topologies; `layered` and `hexagonal`
dependency styles; `technical`, `feature`, and `feature-layered` package layouts; and `single-project`,
`independent-projects`, and `maven-reactor` workspace layouts. Unsupported combinations are rejected; Onion is deferred
until it has a separately verified generation strategy. The generated result must pass the workspace and service checks;
the skill repository does not claim a generated microservices fixture has already passed those checks.

Microservices require at least two user-named services, explicit service boundaries, and service-level validation. The
workspace may use independent Maven projects or a Maven reactor, but Maven aggregation must not create production
dependencies between services.

When architecture is not specified, the skill uses a lightweight assessment only for a meaningful domain or an explicit
request for a recommendation. A technical-only starter uses the default combination without an architecture interview.
The assessment selects values on the three architecture axes and, for microservices, a workspace layout. It does not
introduce another flat list of architecture names.

## Considered Options

- A single flat architecture list: easy to expose, but incorrectly mixes package layout, dependency direction, and module boundaries.
- Arbitrary combinations of independent switches: flexible, but creates many unverified combinations and conflicting directory rules.
