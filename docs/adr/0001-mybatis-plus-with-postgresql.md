# Use MyBatis-Plus with PostgreSQL

Generated Spring Boot 4 projects that require persistence use MyBatis-Plus with PostgreSQL instead of JPA. This keeps SQL mapping explicit while retaining mapper and CRUD conveniences; the choice is a deliberate persistence boundary because replacing it later affects dependencies, mappings, tests, and generated project structure. Projects without persistence do not receive the persistence dependencies or database artifacts; all projects still receive the default Testcontainers test support.

## Considered Options

- Spring Data JPA: more idiomatic for many Spring applications, but adds ORM behavior and entity lifecycle semantics.
- Spring Data JDBC: simpler persistence semantics, but does not provide the selected MyBatis-Plus mapper conventions.
