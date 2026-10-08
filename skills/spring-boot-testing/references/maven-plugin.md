# Maven Test Plugin Configuration

Configure Surefire and Failsafe so that fast tests stay on `mvn test` and everything that needs Docker (Testcontainers, real database, full context) stays on `mvn verify`.

## Naming Convention

| Suffix | Runs in | Plugin | Phase | Typical layers |
| ------ | ------- | ------ | ----- | -------------- |
| `*Test` / `*Tests` | `mvn test` | Surefire | `test` | unit tests, test slices |
| `*IT` | `mvn verify` | Failsafe | `integration-test` + `verify` | Testcontainers, full `@SpringBootTest`, end-to-end |

Default file patterns (verified from the plugins themselves, no need to re-declare them):

- Surefire includes `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java`
- Failsafe includes `**/IT*.java`, `**/*IT.java`, `**/*ITCase.java`

If you follow the naming, the two plugins never overlap and no `includes`/`excludes` list is required.

## Required Configuration

Failsafe is **not** bound to the default Maven lifecycle: without an execution binding, `mvn verify` reports BUILD SUCCESS and runs zero integration tests.

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

`spring-boot-starter-parent` already provides this binding in its `pluginManagement` (plus `classesDirectory` for the repackaged jar). When you inherit from it, declaring the plugin is enough:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
</plugin>
```

Only import the BOM, or use a standalone POM? Write the `executions` yourself.

`failsafe:verify` reads the results recorded by `integration-test` and fails the build there, so never add `-Dmaven.test.failure.ignore` to the `integration-test` execution.

## Optional Excludes (only for deviating names)

Surefire never matches `*IT.java`, so nothing to exclude is left when integration classes follow the convention. Explicit excludes matter only when integration tests use a Surefire-matched suffix such as `*Test`, `*Tests`, or `*TestCase`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <excludes>
      <!-- match how the integration classes are really named -->
      <exclude>**/*IntegrationTests.java</exclude>
      <exclude>**/integration/**</exclude>
    </excludes>
  </configuration>
</plugin>
```

An `<includes>**/*IT.java</includes>` on Failsafe duplicates its own default, so it is documentation rather than configuration. Prefer renaming classes over maintaining exclude lists: the longer the list, the easier it is for one integration test to stop running unnoticed.

## Commands

| Goal | Command |
| ---- | ------- |
| Fast feedback, no Docker | `./mvnw test` |
| Everything | `./mvnw verify` |
| One unit/slice class | `./mvnw test -Dtest=OrderServiceTest` |
| One method | `./mvnw test -Dtest=OrderServiceTest#shouldRejectExpiredCoupon` |
| One integration class | `./mvnw verify -Dit.test=OrderRepositoryIT` |
| Skip integration tests only | `./mvnw verify -DskipITs` |
| Skip both (rarely correct) | `./mvnw verify -DskipTests` |
| Run one class in a multi-module build | append `-Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false` |

Prefer the Maven Wrapper (`./mvnw`) when the project has one so plugin versions match CI.

## Parallel Execution

Configure in `src/test/resources/junit-platform.properties` — no POM change, and the same file is honoured by IDEs and Gradle:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
junit.jupiter.execution.parallel.config.strategy = dynamic
```

Keep methods of one class on the same thread (shared fixtures), run classes concurrently. Turn it on only where you measured a benefit:

- Safe for unit tests and slices (no shared mutable state).
- Integration tests against one shared container need data isolation first (unique keys per test) or `@ResourceLock`.

## Tree Console Output (optional)

Surefire's default output collapses each class into one blob. `maven-surefire-junit5-tree-reporter` prints a per-test tree; it is injected as a plugin dependency, so it never reaches your application classpath.

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <dependencies>
    <dependency>
      <groupId>me.fabriciorby</groupId>
      <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
      <version>1.5.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <forkedProcessExitTimeoutInSeconds>5</forkedProcessExitTimeoutInSeconds>
    <reportFormat>plain</reportFormat>
    <consoleOutputReporter>
      <disable>true</disable>
    </consoleOutputReporter>
    <statelessTestsetInfoReporter
            implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
      <theme>UNICODE</theme>
      <printStacktraceOnError>true</printStacktraceOnError>
      <printStacktraceOnFailure>true</printStacktraceOnFailure>
      <printStdoutOnError>true</printStdoutOnError>
      <printStdoutOnFailure>true</printStdoutOnFailure>
      <printStdoutOnSuccess>false</printStdoutOnSuccess>
      <printStderrOnError>true</printStderrOnError>
      <printStderrOnFailure>true</printStderrOnFailure>
      <printStderrOnSuccess>false</printStderrOnSuccess>
    </statelessTestsetInfoReporter>
  </configuration>
</plugin>
```

- `reportFormat=plain`, the reporter, and `consoleOutputReporter.disable` must appear together, otherwise output is duplicated or empty.
- Suppressing stdout/stderr on success is CI noise control; set `printStdoutOnSuccess` to `true` while diagnosing slowness.
- `theme` accepts `UNICODE`, `ASCII`, `EMOJI` — use `ASCII` on terminals without the glyphs.
- `forkedProcessExitTimeoutInSeconds` (default 30) surfaces a hung forked JVM — common with Testcontainers or JDBC non-daemon threads — as a timeout instead of an infinite wait. A timeout is not a test failure; do not "fix" it by raising the value.
- Merge this with any Surefire configuration above into a single `<plugin>` block; declaring the same plugin twice triggers a Maven warning and one configuration wins.
- To get the same output for integration tests, copy the `<dependencies>` and `<configuration>` into the Failsafe block and verify — plugin configuration is not inherited.

## Coverage with JaCoCo

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.15</version>
  <executions>
    <execution>
      <id>prepare-agent</id>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

JaCoCo reads bytecode, so its release must support your JDK — an old version fails with `Unsupported class file major version`. `prepare-agent` publishes the agent as the `argLine` property, which both Surefire and Failsafe read, so unit and integration coverage append to the same `target/jacoco.exec`. A hardcoded `<argLine>` in either plugin is the usual reason one layer silently vanishes from the report; use `@{argLine}` (late replacement) if you must extend it.

Bind `report` to `verify`, not to `test`: at the `test` phase integration coverage has not been collected yet. Configure the `merge` goal only when you deliberately write to separate `destFile`s.

Use the report to find untested branches. The 80% goal in `SKILL.md` is a floor to sanity-check against, not an acceptance criterion: a percentage is equally satisfied by `assertNotNull(cut)` tests that assert nothing.

## CI Checklist

1. Run `test` first (seconds, no Docker), then `verify`.
2. Assert integration tests actually ran: check the count in `target/failsafe-reports/`. Zero tests with a green build is worse than one red assertion.
3. Never let CI use `-DskipTests`, `@Disabled`, or `@Testcontainers(disabledWithoutDocker = true)` to turn a missing Docker daemon into a pass.
4. Cache `~/.m2/repository` and the container image layers.
5. On failure, keep `target/surefire-reports/`, `target/failsafe-reports/`, and container logs as artifacts.

## Migrating an Existing Project

One commit per step, stop on the first red:

1. Fix naming only: choose `*Test` / `*IT`, rename or tag the Docker-dependent classes. Do not touch assertions.
2. Add the Failsafe execution, confirm `test` no longer needs Docker, and compare the total test count with the migration — a drop means a rename missed a class.
3. Centralize containers and context (`@TestConfiguration` + `@ServiceConnection`, one abstract IT base) — biggest wall-clock win.
4. Push HTTP assertions down from `@SpringBootTest` into slices, one controller at a time.
5. Only then discuss parallelism and coverage.

## Links

- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) - default includes and parameters
- [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) - `it.test`, `skipITs`, report directory
- [JUnit User Guide](https://docs.junit.org/) - parallel execution configuration keys

See also in this skill:

- [test-slices-overview.md](test-slices-overview.md) - which slice proves what
- [context-caching.md](context-caching.md) - configuration signatures and context reuse
- [testcontainers-jdbc.md](testcontainers-jdbc.md) - real database for integration tests
- [sb4-migration.md](sb4-migration.md) - Spring Boot 4 dependency and API changes
