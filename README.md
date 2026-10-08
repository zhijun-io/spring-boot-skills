# spring-boot-skills

[SkillsJars](https://skillsjars.com) packaging for reusable agent skills — starting with Spring Boot testing conventions. Each skill is a `SKILL.md` router plus `references/*.md` documents that hold the detail.

See [SKILLS.md](SKILLS.md) for the catalog.

## Skills Included

| Skill | References |
|-------|------------|
| `spring-boot-testing` | slice overview, `@WebMvcTest`, `@DataJpaTest`, `@RestClientTest`, MockMvc classic + tester, RestTestClient, `@MockitoBean`, AssertJ basics/collections, Instancio, Testcontainers JDBC, context caching, Boot 4 migration, security testing, WebSocket/STOMP testing, maven-plugin |

All content targets **Spring Boot 4.x / Spring Framework 7.x / Spring Security 7.x / JUnit 6**, with Boot 3.x differences called out per file.

## For Consumers

Add the jar as a dependency, then extract the skills into your agent's skills directory:

```xml
<dependency>
  <groupId>io.github.zhijunio</groupId>
  <artifactId>spring-boot-skills</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```bash
# project-local
mvn com.skillsjars:maven-plugin:0.0.7:extract -Ddir=.codex/skills

# global, and named by skill instead of the jar path
mvn com.skillsjars:maven-plugin:0.0.7:extract -Ddir=~/.codex/skills -DuseSkillsNameAsDirectory=true
```

The artifact is not published yet, so install it locally first (`mvn install` in this repo) if you try this before a release exists.

## For Contributors

```
skills/<skill-name>/
├── SKILL.md          — frontmatter (name, trigger-rich description) + routing body
└── references/*.md   — one topic per file, self-contained
```

- `mvn package` copies `skills/**` into `META-INF/skills/<owner>/<repo>/<skill>/`; the build fails for a skill directory without `SKILL.md`.
- Adding `allowed-tools` to a `SKILL.md` requires a matching `skillsjars.skill.<name>.allowed-tools` property in `pom.xml` — the plugin compares them.
- Verify packaging after editing: `mvn -q install` then `jar tf target/*.jar | grep META-INF/skills`.
- Facts in reference files must be reproducible: state the version they were checked against, and prefer "this fails with X" over folklore.

## Status

`0.1.0-SNAPSHOT`, no CI, no release pipeline. Adding a skill means a new directory under `skills/` and a row in [SKILLS.md](SKILLS.md).
