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
- Facts in reference files must be reproducible: state the version they were checked against, and prefer "this fails with X" over folklore.
- Verify locally what CI does not cover — CI only builds the jar:

```bash
./mvnw -B clean verify                                             # what CI runs
jar tf target/spring-boot-skills-*.jar | grep '^META-INF/skills/'  # every file shipped, nothing missing
./mvnw -q -B install
```

Then, from any project that depends on `io.github.zhijunio:spring-boot-skills`, run
`mvn com.skillsjars:maven-plugin:0.0.7:extract -Ddir=.codex/skills`. That extraction is the only
end-to-end proof the jar is usable — packaging can look correct and still be invisible to an agent.

Relative links from `SKILL.md` into `references/*.md` are worth a once-over as well: agents follow them, and a dead link drops context without any error.

## CI and Publishing

| Workflow | Trigger | What it runs |
| -------- | ------- | ------------ |
| `ci.yml` | push and pull request to `main` | `ci-build.yml` from `spring-ai-community/community-workflows`: JDK 17 (temurin), Maven cache, `./mvnw clean verify -B` |
| `publish-snapshot.yml` | push to `main`, manual dispatch | `./mvnw deploy` of the snapshot, with `MAVEN_USERNAME` / `MAVEN_PASSWORD` |
| `release.yml` | manual dispatch with `version` | sets the version, verifies, `./mvnw deploy -Prelease`, then tags — also needs `GPG_SECRET_KEY` / `GPG_PASSPHRASE` |

All three delegate to that shared workflow repository, mirroring how `spring-testing-skills` is set up. Two consequences: they invoke `./mvnw`, so the wrapper is part of the contract; and they track `@main` of an external repository, so an upstream change can break CI here.

Publishing is wired but not usable yet:

- `pom.xml` has no `distributionManagement` and no `release` profile (sources jar, javadoc jar, GPG signing, Central publishing plugin), and Central requires `licenses` and `developers` — none of which is present.
- The `MAVEN_*` and `GPG_*` secrets are not configured, and the `io.github.zhijunio` namespace must be registered before the first release.
- `<url>` and `<scm>` point at `github.com/zhijunio/...` while `origin` is `github.com:zhijun-io/spring-boot-skills`. The path inside the jar comes from `<scm>`, so skills currently land under `META-INF/skills/zhijunio/spring-boot-skills/...`. Align one side with the other before publishing, or consumers get a misleading location.
- `ci.yml` does not pass `upload-test-results: false`, so that step warns about a missing `target/surefire-reports/` — there is no Java source in this repository.

## Status

`0.1.0-SNAPSHOT`. CI verifies the jar build on every pull request; the snapshot and release workflows exist but are not configured. Nothing is published yet, so use `./mvnw install` for local consumption. Adding a skill means a new directory under `skills/` and a row in [SKILLS.md](SKILLS.md).
