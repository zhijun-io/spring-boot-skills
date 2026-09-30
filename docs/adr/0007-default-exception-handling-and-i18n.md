# Default Exception Handling and Internationalization

Generated applications include centralized `@RestControllerAdvice` exception handling and Spring message resolution by default. Validation, malformed requests, declared application failures, and unexpected failures become Problem Details responses with stable error identifiers; message text is resolved through `MessageSource` and `Accept-Language`, with `messages.properties` as the fallback bundle.

This keeps error contracts consistent across controllers and gives API clients localized messages without exposing internal exception details. Locale-specific bundles and domain messages are added only when the request defines the supported locales or business terminology.

## Considered Options

- Controller-local exception handling: small initially, but duplicates response contracts and localization behavior.
- Raw exception responses: simple, but leaks implementation details and produces unstable client contracts.
- Centralized Problem Details plus MessageSource: explicit response behavior with one localization boundary and a controlled fallback.
