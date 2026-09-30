# Require Scoped Mutation and Explicit Approval

The migration skill may inspect a dirty working tree, but `apply` changes only files explicitly included in an approved plan
and never overwrites unrelated uncommitted work. Planning and applying are separate invocations; editing the migration
report does not grant approval. External dependencies that are unavailable are reported as unverified rather than hidden
behind substitutes.
