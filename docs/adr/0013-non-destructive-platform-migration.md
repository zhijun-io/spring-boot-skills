# Keep Platform Migration Non-Destructive by Default

The platform migration stage may update build metadata, dependencies, configuration, source compatibility, and migration
tests, but it must not change database schema or data, invent business tests, or infer behavior for ambiguous APIs. Such
changes are separate explicit stages because they have different rollback and compatibility risks. Unresolved concerns are
reported as migration risks and stop `apply` at the first actionable root cause.
