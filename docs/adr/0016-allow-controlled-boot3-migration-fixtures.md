# Allow Controlled Spring Boot 3 Migration Fixtures

The project-generation skill remains Boot 4-first for ordinary new applications. It may create a clearly named Maven
Spring Boot 3.x fixture only when the user explicitly needs an acceptance source for `spring-boot-migration`.

Current Spring Initializr metadata may stop listing the older Boot line even though the released artifacts remain available.
For this exception, verify the exact parent and dependency BOM from the official Maven repository, avoid snapshots, keep the
fixture under `examples/`, and document its non-production purpose. This preserves the normal new-project baseline while
making the migration workflow testable against a real older platform.
