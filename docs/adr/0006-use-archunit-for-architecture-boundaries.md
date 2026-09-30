# Use ArchUnit for Architecture Boundaries

Generated projects include `com.tngtech.archunit:archunit-junit5` in test scope and generate architecture tests for the selected supported architecture combination. This makes the topology, dependency style, and package layout executable constraints while keeping ArchUnit out of the runtime artifact. The ArchUnit version is resolved as a current version compatible with the selected Spring Boot test stack rather than fixed by this ADR.

The selected Spring Boot version must be checked against the resolved ArchUnit and JUnit test stack during generation.

## Considered Options

- Documentation-only boundaries: easy to generate, but violations remain invisible until review or runtime failures.
- Runtime dependency checks: operate against live wiring, but add application complexity and do not replace static dependency checks.
- ArchUnit tests: add a small test dependency and maintenance cost, while making package and dependency direction failures visible in the normal test phase.
