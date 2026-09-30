# Treat Related Dependencies as Separate Migration Phases

A Spring Boot major-version migration changes the managed dependency graph, but applications also depend on Spring
ecosystem projects, persistence libraries, database drivers, serialization/API tools, test infrastructure, build plugins,
and private libraries. The migration skill must inventory these dependencies and assess their target compatibility instead
of treating the Boot parent version as the whole migration.

Each used ecosystem gets its own rule, evidence, and validation. OpenRewrite recipes may execute deterministic portions, but
major-version behavior changes remain `review`. Schema/data changes, public contract changes, security changes, and
unverified private dependencies remain outside the platform phase or block `apply` until explicitly scoped.
