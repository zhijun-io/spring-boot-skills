# Use Header-Based API Versioning When Requested

API version negotiation is optional. When a project requires it, generated APIs keep resource URLs such as `/api/{resource}` free of version segments and use Spring MVC API versioning through the `X-Version` request header, defaulting to `1.0.0`. This follows Spring Boot 4.1.1's supported configuration and leaves resource URLs stable while allowing multiple controller versions to be assembled later.

## Considered Options

- URL versioning: easy to inspect, but duplicates resource paths for each version.
- Media-type versioning: expressive, but less visible and more cumbersome for ordinary clients.
- Query-parameter versioning: simple to call, but treats version as a request filter rather than part of the representation contract.
