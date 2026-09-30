# Default Exception Handling and Internationalization

Generated applications include centralized `@RestControllerAdvice` exception handling and Spring message resolution by default. Validation, malformed requests, declared application failures, and unexpected failures become Problem Details responses with stable error identifiers; message text is resolved through `MessageSource` and `Accept-Language`, with `messages.properties` as the fallback bundle.

This keeps error contracts consistent across controllers and gives API clients localized messages without exposing internal exception details. Locale-specific bundles and domain messages are added only when the request defines the supported locales or business terminology.

The advice must have an explicit precedence strategy when Spring's built-in Problem Details resolver is also active. A
broad fallback handler must not swallow a declared application exception or replace the stable code for malformed input.
Generated API tests verify malformed input, validation, declared failures, and the generic fallback response at the HTTP
boundary, including status, `application/problem+json`, stable `code`, and localized `detail`.

## Considered Options

- Controller-local exception handling: small initially, but duplicates response contracts and localization behavior.
- Raw exception responses: simple, but leaks implementation details and produces unstable client contracts.
- Centralized Problem Details plus MessageSource: explicit response behavior with one localization boundary and a controlled fallback.
