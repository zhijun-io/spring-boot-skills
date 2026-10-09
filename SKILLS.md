# Skills Catalog

Agent-facing catalog of the skills in this repository. Each skill's `SKILL.md` frontmatter `description` decides when an agent loads it.

## Skills

| Skill | Covers | Load When |
|-------|--------|-----------|
| `spring-boot-testing` | Test pyramid and slice selection, `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, `@RestClientTest`, `@SpringBootTest`, MockMvc vs `MockMvcTester`, `RestTestClient`, `@MockitoBean`, AssertJ, Instancio, Testcontainers + `@ServiceConnection`, context caching, Spring Security slices (`@WithMockUser`, `jwt()`, chain import), STOMP/WebSocket over `RANDOM_PORT`, MyBatis-Plus slice `@MybatisPlusTest` (mapper scan, pagination interceptor, batch vs mocked service tests), Maven Surefire/Failsafe and JaCoCo setup | Writing, reviewing or fixing tests in a Spring Boot 4 project; choosing a test layer; test build configuration; MyBatis-Plus mapper/service tests; slow or flaky test suites |

## Routing Notes

- Security annotations (`@WithMockUser`, `@WithUserDetails`, `jwt()`) stay in `spring-boot-testing` even when combined with `@WebMvcTest` — the slice alone is not the topic.
- STOMP/WebSocket tests use `@SpringBootTest(RANDOM_PORT)`, but the reasoning lives in `references/websocket-testing.md`.
- Build-level questions ("why did `mvn test` skip my `*IT`?") route to `references/maven-plugin.md`, not to a slice reference.
- MyBatis-Plus persistence questions (`@MybatisPlusTest`, lambda wrappers, `PaginationInnerInterceptor`) route to `references/mybatis-plus-testing.md`; JPA slices stay in `datajpatest.md`.
