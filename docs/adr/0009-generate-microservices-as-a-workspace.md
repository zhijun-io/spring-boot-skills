# Generate Microservices as a Workspace

Microservices are generated as a workspace containing at least two independently runnable services, rather than as
packages inside one Spring Boot application. The default workspace layout is one independent Maven project per service;
`maven-reactor` is opt-in for teams that want a unified root build and dependency management without sharing business
code. This preserves service boundaries while supporting both repository workflows.

The root workspace may contain documentation, infrastructure Compose configuration, and selected developer tooling. It
does not receive a gateway, service discovery, configuration server, broker, shared business library, or shared database
tables unless the request defines those contracts explicitly.
