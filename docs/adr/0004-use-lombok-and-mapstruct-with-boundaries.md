# Use Lombok and MapStruct with Explicit Boundaries

Generated projects include Lombok and MapStruct to reduce repetitive model code and keep mappings explicit, despite the additional build-time dependencies. Lombok usage is limited to local accessors, builders, and constructors rather than `@Data`; MapStruct maps between API DTOs, application commands/results, and persistence objects, and persistence objects are not exposed directly as API responses.

## Considered Options

- Plain Java with handwritten mapping: fewer dependencies, but more repetitive generated-project code.
- Unrestricted Lombok and automatic mapping: shorter code, but hides object contracts and can leak persistence details across boundaries.
