# Use Spring Boot 4 Testcontainers and Test Package Conventions

Generated Spring Boot 4 projects use Spring Boot's managed Testcontainers versions. PostgreSQL integration tests use
`org.testcontainers:testcontainers-postgresql` together with `org.testcontainers:testcontainers-junit-jupiter`; the
old `org.testcontainers:postgresql` coordinate is not valid for the selected Testcontainers line. Persistence-backed
context tests must bind a PostgreSQL container with `@ServiceConnection` or deliberately run the generated Compose
service, so the default test suite cannot depend on an undeclared localhost database. After Maven resolves the selected
line, the generated code must verify the actual PostgreSQL container package, constructors, generic signature, and
lifecycle API in the resolved JAR; these details are version-sensitive and must not be copied from an older example.

Use one lifecycle mechanism per test context: Spring-managed `@Bean` plus `@ServiceConnection`, or JUnit-managed
`@Container` plus `@Testcontainers`. Test the complete integration path, not only container startup.

Boot 4 MVC test configuration uses `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`. ArchUnit
continues to use the `com.tngtech.archunit:archunit-junit5` artifact, but its JUnit annotations are imported from
`com.tngtech.archunit.junit`.
