# spring-boot-skills

[SkillsJars](https://skillsjars.com) packaging for reusable agent skills — starting with Spring Boot testing conventions. Each skill is a `SKILL.md` router plus `references/*.md` documents that hold the detail.

See [SKILLS.md](SKILLS.md) for the catalog.

## Skills Included

| Skill | References |
|-------|------------|
| `spring-boot-testing` | slice overview, `@WebMvcTest`, `@DataJpaTest`, `@RestClientTest`, MockMvc classic + tester, RestTestClient, `@MockitoBean`, AssertJ basics/collections, Instancio, Testcontainers JDBC, context caching, Boot 4 migration, security testing, WebSocket/STOMP testing, maven-plugin |

All content targets **Spring Boot 4.x / Spring Framework 7.x / Spring Security 7.x / JUnit 6**, with Boot 3.x differences called out per file.

## Installation

Skills install into an agent's skills directory: `~/.codex/skills` (or `.codex/skills` in a project) for Codex, `~/.claude/skills` (or `.claude/skills`) for Claude Code. Three ways to get them there:

### Install with npx

The [skills CLI](https://skills.sh) clones this repository, finds every `SKILL.md`, and links the skills into your agents' skills directories:

```bash
# project skills in the current directory
npx skills add zhijun-io/spring-boot-skills

# a single skill, globally, for Codex only
npx skills add zhijun-io/spring-boot-skills -s spring-boot-testing -g -a codex
```

`-l` lists what the repository ships without installing; `--copy` writes real files instead of symlinks.

### Copy from the repository

```bash
git clone https://github.com/zhijun-io/spring-boot-skills.git
cp -r spring-boot-skills/skills/spring-boot-testing ~/.codex/skills/
```

Each directory under `skills/` is a self-contained skill (`SKILL.md` plus `references/`), so a plain copy is enough — no build step.

### Extract from the SkillsJars jar

Add the jar as a dependency, then extract the skills:

```xml
<dependency>
  <groupId>io.github.zhijun-io</groupId>
  <artifactId>spring-boot-skills</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```bash
# project-local
./mvnw com.skillsjars:maven-plugin:extract -Ddir=.codex/skills

# global, and named by skill instead of the jar path
./mvnw com.skillsjars:maven-plugin:extract -Ddir=~/.codex/skills -DuseSkillsNameAsDirectory=true
```

Omit the plugin version. Maven takes it from your `pom.xml` when the plugin is
declared there, and otherwise from the newest release in the repository — so this
command does not go stale when the plugin version moves up.

The artifact is not published yet, so install it locally first (`./mvnw install`) if you try this before a release exists.

Whichever method you use, restart the agent session so the new skill is picked up.

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

Then, from any project that depends on `io.github.zhijun-io:spring-boot-skills`, run
`mvn com.skillsjars:maven-plugin:extract -Ddir=.codex/skills`. That extraction is the only
end-to-end proof the jar is usable — packaging can look correct and still be invisible to an agent.

Relative links from `SKILL.md` into `references/*.md` are worth a once-over as well: agents follow them, and a dead link drops context without any error.

## CI and Publishing

| Workflow | Trigger | What it runs |
| -------- | ------- | ------------ |
| `ci.yml` | push and pull request to `main` | `ci-build.yml` from `zhijun-io/github-workflows`: JDK 17 (temurin), Maven cache, `./mvnw clean verify -B` |
| `publish-snapshot.yml` | push to `main`, manual dispatch | `./mvnw deploy` of the snapshot, with `MAVEN_USERNAME` / `MAVEN_PASSWORD` |
| `release.yml` | manual dispatch with `version` | sets the version, verifies, `./mvnw deploy -Prelease`, then tags — also needs `GPG_SECRET_KEY` / `GPG_PASSPHRASE` |

All three delegate to that shared workflow repository, mirroring how `spring-testing-skills` is set up. Two consequences: they invoke `./mvnw`, so the wrapper is part of the contract; and they track `@main` of an external repository, so an upstream change can break CI here.

The build configuration is not in this POM — it is inherited from
[`io.github.zhijun-io:rose-parent:0.0.1`](https://github.com/zhijun-io/rose-parent), the shared parent POM.
The empty `<relativePath/>` is deliberate: the parent is a released artifact resolved as a dependency, not a
sibling directory that happens to sit next to this checkout.
What the parent provides:

- the `release` profile: `central-publishing-maven-plugin` with `autoPublish`, GPG signing with loopback pinentry, sources and javadoc jars
- `distributionManagement` for snapshots
- plugin versions (`maven-compiler`, `surefire`, `jar`, `deploy`, `source`, `javadoc`, `flatten`, `spring-javaformat`) and `java.version`
- the `licenses` and `developers` blocks Central requires

What this POM keeps: the SkillsJars plugin and `skillsjars.version`, its own `<url>` and `<scm>` (the path inside the jar comes from `<scm>`, so it must point at this repository), the `skills/` tree and the docs.

`rose-parent:0.0.1` is released, but so far only into the private repository manager behind the developer's
`~/.m2/settings.xml` (`wesine-releases` / `wesine-snapshots`). Consequences:

- A GitHub-hosted runner has neither that `settings.xml` nor that artifact in `~/.m2`, so CI cannot resolve the
  parent until `rose-parent` is on Maven Central or the runner is given equivalent settings and credentials.
  A parent POM cannot declare the repository it lives in, so this is a settings-level concern, not a pom fix.
- Consumers outside that network cannot resolve `spring-boot-skills` either: its deployed POM references a
  parent they cannot fetch. Publishing the child to Central before the parent is there produces a POM that is
  technically valid and practically unusable.
- `MAVEN_*` / `GPG_*` secrets are not configured, and the `io.github.zhijun-io` namespace must be verified on Central.

I verified build, packaging and extraction locally (including offline parent resolution); none of these
workflows have been run on GitHub.

## Status

`0.1.0-SNAPSHOT`, coordinates `io.github.zhijun-io:spring-boot-skills`. Nothing is published yet, so build and `./mvnw install` locally, then extract. Adding a skill means a new directory under `skills/` and a row in [SKILLS.md](SKILLS.md).

The groupId changed from `io.github.zhijunio` to `io.github.zhijun-io` when this project started inheriting `rose-parent`. Anything that referenced the old coordinate — including an already-extracted skill directory under a `zhijunio` path — needs re-extracting.
