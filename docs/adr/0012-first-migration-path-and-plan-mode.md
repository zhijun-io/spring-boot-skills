# Start With Maven Spring Boot 3.x to 4.x in Plan Mode

The first migration path is a Maven-based Spring Boot 3.x application moving to Spring Boot 4.x. The skill defaults to a
`plan` mode that inspects the project and writes a migration report without changing source files; an explicit `apply` mode
executes the approved scope. This keeps the initial implementation focused and makes version, build-system, and behavior
compatibility decisions visible before mutation.

The default compatibility contract preserves REST contracts, database schema and data, authentication semantics,
configuration meaning, and business-test behavior. Gradle, Spring Boot 2.x, non-Spring applications, destructive schema
changes, and architecture rewrites are deferred rather than inferred.
