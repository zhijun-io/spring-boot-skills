# Require Explicit Business Requirements

The project creation skill does not generate a default business domain or example entities. When the request lacks enough information to define resources, fields, relationships, and operations, the skill asks for those requirements before generating domain code. This keeps the generated project aligned with the user's domain instead of embedding an arbitrary sample application.

## Considered Options

- A default CRUD example: runnable, but it disguises an arbitrary domain as a project baseline.
- A fixed multi-domain example: exercises more boundaries, but adds domain assumptions and unrelated code.
