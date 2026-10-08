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
./mvnw com.skillsjars:maven-plugin:0.0.7:extract -Ddir=.codex/skills

# global, and named by skill instead of the jar path
./mvnw com.skillsjars:maven-plugin:0.0.7:extract -Ddir=~/.codex/skills -DuseSkillsNameAsDirectory=true
```

The artifact is not published yet, so install it locally first (`./mvnw install`) if you try this before a release exists.

## For Contributors

```
skills/<skill-name>/
├── SKILL.md          — frontmatter (name, trigger-rich description) + routing body
└── references/*.md   — one topic per file, self-contained
```

- `./mvnw package` copies `skills/**` into `META-INF/skills/<owner>/<repo>/<skill>/`; the build fails for a skill directory without `SKILL.md`.
- Adding `allowed-tools` to a `SKILL.md` requires a matching `skillsjars.skill.<name>.allowed-tools` property in `pom.xml` — the plugin compares them.
- Run the same checks CI runs, before pushing:

```bash
python3 .github/scripts/validate-skills.py                    # layout, frontmatter, links, catalog
./mvnw -B clean verify                                        # build the jar
jar tf target/spring-boot-skills-*.jar | grep META-INF/skills  # packaging
```

- Facts in reference files must be reproducible: state the version they were checked against, and prefer "this fails with X" over folklore.

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on pushes and pull requests to `main`:

1. `validate-skills.py` — every `skills/<name>/SKILL.md` exists with a `name` matching its directory, a non-empty `description` under 1024 characters, a body, no dead relative links, and a row in `SKILLS.md`.
2. `./mvnw clean verify` on JDK 17, then a `diff` proving the files in `META-INF/skills/` are exactly the files under `skills/`.
3. A consumer smoke test: install the jar, generate a one-line consumer `pom.xml`, run `skillsjars:extract`, and assert the extracted `SKILL.md` and reference files land in the target directory.

The built jar is uploaded as a workflow artifact. Publishing to Maven Central is not configured — that needs `licenses`/`developers` in `pom.xml`, sources and javadoc jars, signing, and `MAVEN_USERNAME`/`MAVEN_PASSWORD` (plus `GPG_*`) secrets.

## Status

`0.1.0-SNAPSHOT`, CI builds and verifies on every pull request, no release pipeline yet. Adding a skill means a new directory under `skills/` and a row in [SKILLS.md](SKILLS.md).
