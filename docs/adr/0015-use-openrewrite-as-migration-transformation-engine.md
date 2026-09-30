# Use OpenRewrite as the Migration Transformation Engine

For the supported Maven Spring Boot 3.x to 4.x migration, use OpenRewrite as the preferred execution engine when a
versioned recipe covers the requested transformation. The migration skill remains responsible for baseline inspection,
scope decisions, compatibility classification, approval, and validation.

OpenRewrite provides structured Java, Maven, and configuration changes and can produce a dry-run patch before mutation.
This reduces ad hoc text editing and makes repeatable transformations inspectable. It does not prove business behavior,
security semantics, database compatibility, third-party support, or deployment compatibility.

`plan` resolves exact plugin and recipe coordinates and runs a dry-run. The generated patch is classified per change as
`safe`, `review`, or `blocker`. `apply` runs only explicitly approved recipes and file scopes, then validates the result
with Maven and relevant real infrastructure. Missing recipes, unavailable recipe repositories, authentication failures, or
patches outside the approved scope remain risks or blockers; the skill does not silently fall back to broad text replacement.
