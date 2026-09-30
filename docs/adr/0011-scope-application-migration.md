# Scope Application Migration Separately From Project Generation

The migration skill will operate on an existing Spring Boot application and default to platform migration: Java, Spring
Boot, managed dependencies, related dependency compatibility, configuration, build compatibility, and tests. Related
dependency ecosystems are assessed and migrated in separate phases. Architecture, persistence/schema/data, breaking API,
and deployment changes remain explicit later stages so a version upgrade does not silently become an architectural rewrite.

The skill will preserve uncommitted work, record a migration baseline, modify the existing project only after inspection,
and produce a migration report with validation results and unresolved risks. A failed migration keeps the changed files and
stops at the first actionable root cause rather than repeatedly rewriting the project.
