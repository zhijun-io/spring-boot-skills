# Spring Security Testing

Testing authentication and authorization in Spring Boot 4.x with Spring Security 7.x. Every API below was checked against `spring-security-test:7.1.1` and the Boot 4.1.1 test starters.

## Dependency

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

This pulls in `spring-boot-security-test`, which registers `SecurityAutoConfiguration`, `UserDetailsServiceAutoConfiguration`, `SecurityFilterAutoConfiguration`, and `ServletWebSecurityAutoConfiguration` for MockMvc slices and makes `spring-security-test` available. Adding `spring-security-test` by hand is unnecessary and duplicates the managed version. `@WebMvcTest` also needs `spring-boot-starter-webmvc-test`; without it the slice annotation does not compile.

### The slice loads a chain, but not necessarily yours

`@WebMvcTest` auto-configures Spring Security, so anonymous requests get `401` and `@WithMockUser` works — which makes it look like your rules are under test. They are not. Slice scanning includes components that *implement* `SecurityFilterChain`, but your usual shape is a `@Configuration` class with a `@Bean SecurityFilterChain` method, and that class is outside the scan. The slice then runs Boot's default chain: every request merely authenticated, no role or authority rules at all.

Observed with a chain that requires `hasAuthority("reports:read")` on `/reports/**`:

| Setup | `@WithMockUser(roles = "USER")` | Expected |
| ----- | ------------------------------- | -------- |
| `@WebMvcTest(ReportController.class)` | `200` | `403` |
| `@WebMvcTest(ReportController.class)` + `@Import(ReportSecurityConfig.class)` | `403` | `403` |

Wrong-role requests returning `200` is the signal: the test is green because the rule was never loaded. Import the configuration under test, and assert the rejected case explicitly so a missing rule cannot pass:

```java
@WebMvcTest(ReportController.class)
@Import(ReportSecurityConfig.class)
class ReportControllerSecurityTest {

  @Autowired private MockMvc mvc;

  @Test
  @WithMockUser(username = "alice", roles = "USER")
  void wrongAuthorityIsForbidden() throws Exception {
    mvc.perform(get("/reports/42")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "alice", authorities = "reports:read")
  void correctAuthorityIsAllowed() throws Exception {
    mvc.perform(get("/reports/42")).andExpect(status().isOk());
  }

  @Test
  void anonymousIsUnauthorized() throws Exception {
    mvc.perform(get("/reports/42")).andExpect(status().isUnauthorized());
  }
}
```

The `200` and `403` pair above is what makes the third test meaningful: a slice that only asserts `200` proves nothing about authorization.

## Pick the Right Layer

| Rule to prove | Where |
| ------------- | ----- |
| URL-level rules (`/admin/**` requires ADMIN), 401 vs 403, CSRF enforcement | Slice with the real `SecurityFilterChain` |
| Method security (`@PreAuthorize`), SpEL on parameters | `@SpringBootTest` with a minimal context, or a plain unit call |
| Resource ownership and tenant isolation (does principal X own resource Y?) | Integration test with the real database |
| Password encoding, token issuing/validation logic | Plain JUnit — no Spring |
| Full chain behaviour with real tokens and filters | `@SpringBootTest` + `@AutoConfigureMockMvc`, or `RANDOM_PORT` |

Do not weaken a production rule to make a test pass; add the missing principal or authority to the test instead.

## @WithMockUser — Synthetic Principal

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

@WebMvcTest(ReportController.class)
@Import(ReportSecurityConfig.class)
class ReportControllerSecurityTest {

  @Autowired private MockMvcTester mvc;

  @Test
  @WithMockUser(username = "alice", authorities = "reports:read")
  void readerGetsReport() {
    assertThat(mvc.get().uri("/reports/42"))
        .hasStatus(OK)
        .matches(authenticated().withUsername("alice"));
  }

  @Test
  @WithMockUser(username = "alice", roles = "USER")
  void nonReaderIsForbidden() {
    assertThat(mvc.get().uri("/reports/42")).hasStatus(FORBIDDEN);
  }
}
```

`MockMvcTester.perform(RequestBuilder)` returns `MvcTestResult`, which has no `expectStatus()` — that matcher chain belongs to classic MockMvc. Use `assertThat(...).hasStatus(...)` / `hasStatusForbidden()`, or the request-builder form `mvc.get().uri("/x").with(postProcessor()).exchange()`. See [mockmvc-tester.md](mockmvc-tester.md).

The `ROLE_` prefix trap:

```java
// roles = "ADMIN"      -> authority ROLE_ADMIN  (prefix added)
// authorities = "ADMIN" -> authority ADMIN      (no prefix added)

@WithMockUser(roles = "ADMIN")            // matches hasRole("ADMIN")
@WithMockUser(authorities = "reports:read") // matches hasAuthority("reports:read")
```

`@WithMockUser(roles = "ROLE_ADMIN")` does not quietly become `ROLE_ROLE_ADMIN` — it fails at context setup, so the mistake cannot hide:

```
java.lang.IllegalStateException: Unable to create SecurityContext using
    @WithMockUser(roles={"ROLE_ADMIN"}, username="alice", ...)
Caused by: java.lang.IllegalArgumentException: roles cannot start with ROLE_ Got ROLE_ADMIN
```

Attributes available in 7.1.1: `value`, `username`, `roles`, `authorities`, `password`, `setupBefore`. `setupBefore` takes `TestExecutionEvent.TEST_METHOD` (the default) or `TEST_EXECUTION`; `TEST_EXECUTION` is the earlier of the two, which is what a `@BeforeEach` method that reads the principal needs.

## Writing the Chain So It Is Testable

A `@Configuration` class that declares `@Bean SecurityFilterChain` is not an MVC component, so `@WebMvcTest` does not create it. The slice's include list covers `WebSecurityConfigurer`, `SecurityCustomizer` and `SecurityFilterChain` *types*; a configuration class matches none of them, which is why Boot's default chain shows up instead. Declare the chain the normal way and let the test name it:

```java
import org.springframework.security.config.Customizer;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class ReportSecurityConfig {

  @Bean
  SecurityFilterChain reportsFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/reports/**").hasAuthority("reports:read")
        .anyRequest().authenticated());
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
```

```java
@WebMvcTest(ReportController.class)
@Import(ReportSecurityConfig.class)
```

Because the rule is written with `hasAuthority`, the test must carry exactly that string — `roles` would produce `ROLE_USER` and miss:

```java
@WithMockUser(authorities = "reports:read")   // granted
@WithMockUser(roles = "USER")                  // ROLE_USER -> 403
```

## Per-Request Post-Processors

`SecurityMockMvcRequestPostProcessors` covers request-level authentication without annotations. Available factories in 7.1.1: `user`, `httpBasic`, `digest`, `anonymous`, `authentication`, `securityContext`, `testSecurityContext`, `csrf`, `x509`, `jwt`, `opaqueToken`, `oauth2Login`, `oidcLogin`, `oauth2Client`. `formLogin(...)` and `logout(...)` live in `SecurityMockMvcRequestBuilders`, not here.

```java
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(ReportSecurityConfig.class)
class ReportControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  void readerUsesPostProcessor() throws Exception {
    mvc.perform(get("/reports/42").with(user("alice").authorities(new SimpleGrantedAuthority("reports:read"))))
       .andExpect(status().isOk());
  }
}
```

The same request with `user("alice").roles("USER")` gets `403`, and with `anonymous()` gets `401`.

Prefer post-processors when the principal changes per method, and `@WithMockUser` when the whole class shares one.

Mutating requests need CSRF or the request is rejected before it reaches the controller:

```java
mvc.perform(post("/orders").with(csrf()).with(user("alice").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON).content(orderJson))
   .andExpect(status().isCreated());
```

Omit `with(csrf())` and the request never reaches the handler — `403` from `AccessDeniedHandler`, not a validation failure. That distinction is worth one dedicated test, because it is the most common reason a mutating test looks broken for no reason.

For token-based APIs, CSRF is normally switched off in the production chain — stateless requests have no session to protect. Keep that rule in the configuration under test rather than adding a test-only `SecurityFilterChain` that disables it.

## JWT Resource Server

`jwt()` builds a `Jwt` without contacting an authorization server. Two prerequisites make or break it:

- the chain under test must configure `oauth2ResourceServer(o -> o.jwt(...))`; against a chain that does not, a `jwt()` request returned `404` while an anonymous request got `403` — the token is simply never converted, so nothing about your rules is proven;
- in a slice, a `JwtDecoder` bean must exist. `@MockitoBean private JwtDecoder jwtDecoder;` is enough — the post-processor supplies the `Jwt`, decoding is not involved.

```java
mvc.perform(get("/me").with(jwt().jwt(j -> j.subject("alice"))))
   .andExpect(status().isOk());

// scope claim -> SCOPE_* authorities
mvc.perform(get("/reports").with(jwt().jwt(j -> j.claim("scope", "reports:read"))))
   .andExpect(status().isOk());

// or set authorities directly
mvc.perform(get("/reports")
        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_reports:read"))))
   .andExpect(status().isOk());

mvc.perform(get("/reports").with(jwt()))
   .andExpect(status().isForbidden());
```

The default converter reads `scope` (space-separated) and prefixes `SCOPE_`. A token carrying `scp` instead — common with authorization servers configured that way — yields `403` until the test also installs the matching `JwtAuthenticationConverter`, which is exactly the bug worth catching in a slice test.

`jwt()` also accepts a fully built `Jwt` and a `Converter<Jwt, Collection<GrantedAuthority>>`, which is how you test a custom `JwtAuthenticationConverter`.

For opaque tokens use `opaqueToken()`; for OAuth2 client logins use `oauth2Login()`, `oidcLogin()`, and `oauth2Client(registrationId)`.

Naming: the servlet-side factory is `oidcLogin()`; `mockOidcLogin()` exists only in `SecurityMockServerConfigurers` for WebFlux. Using the reactive name in a MockMvc test is a compile error, not a behaviour difference.

## Reactive Tests

```java
WebTestClient client = WebTestClient.bindToApplicationContext(context)
    .configureClient()
    .baseUrl("/api")
    .build();

client.get().uri("/reports/42")
    .mutateWith(mockJwt().jwt(j -> j.subject("alice").claim("scope", "reports:read")))
    .exchange()
    .expectStatus().isOk();
```

Reactive factories in 7.1.1: `springSecurity`, `mockAuthentication`, `mockUser`, `mockJwt`, `mockOpaqueToken`, `mockOAuth2Login`, `mockOidcLogin`, `mockOAuth2Client`, `csrf`.

## Assert the Authentication, Not Just the Status

```java
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

mvc.perform(get("/profile").with(user("alice")))
   .andExpect(authenticated().withUsername("alice").withRoles("USER"));

mvc.perform(get("/public")).andExpect(unauthenticated());

// formLogin(String) takes the processing URL, not a username
mvc.perform(formLogin("/login").user("alice").password("password"))
   .andExpect(status().isFound());
```

`formLogin()` already carries a CSRF token and posts to the login URL, so `302` (the default success handler) is the expected outcome — assert `authenticated()` only when the chain redirects somewhere you also control. Available matchers: `authenticated()` with `withUsername`, `withRoles`, `withAuthorities`, `withAuthentication`, `withAuthenticationPrincipal`, `withSecurityContext`, and `unauthenticated()`.

Status codes alone under-prove a security test: `authenticated().withUsername("alice")` distinguishes "the filter chain accepted my principal" from "some principal happened to be present".

## @WithUserDetails — Real Principal Type

Use it when the controller reads `Authentication.getPrincipal()` and expects your own user type rather than a mock.

```java
@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  @WithUserDetails(value = "alice@example.com", userDetailsServiceBeanName = "userDetailsService")
  void returnsAuthenticatedProfile() throws Exception {
    mvc.perform(get("/profile")).andExpect(status().isOk());
  }

  @TestConfiguration
  static class Users {
    @Bean
    UserDetailsService userDetailsService() {
      return new InMemoryUserDetailsManager(
          User.withUsername("alice@example.com").password("{noop}password").roles("USER").build());
    }
  }
}
```

A `@TestConfiguration` nested in the test class is picked up automatically; a top-level one needs `@Import`. With `@WithUserDetails`, the referenced `UserDetailsService` supplies real authorities, so a rule written against `hasAuthority(...)` behaves as it does in production.

## Repeated Token Shapes: Custom @WithSecurityContext

When the same JWT structure appears in many tests, replace repetition with an annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TenantJwtSecurityContextFactory.class)
public @interface WithTenantJwt {
  String tenant();
  String[] authorities() default {};
}

class TenantJwtSecurityContextFactory implements WithSecurityContextFactory<WithTenantJwt> {

  @Override
  public SecurityContext createSecurityContext(WithTenantJwt annotation) {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .subject("user-" + annotation.tenant())
        .claim("tenant_id", annotation.tenant())
        .build();

    List<GrantedAuthority> authorities = Arrays.stream(annotation.authorities())
        .map(SimpleGrantedAuthority::new)
        .toList();

    return new SecurityContextImpl(new JwtAuthenticationToken(jwt, authorities));
  }
}
```

The factory exists to describe one principal shape once. If it starts branching per test, the branch belongs in the annotation attributes.

## Method Security

`@PreAuthorize` is applied by a proxy on the service, not by the filter chain, so an MVC slice never proves it.

```java
@SpringBootTest(classes = {ReportService.class, MethodSecurityConfiguration.class})
class ReportServiceMethodSecurityTest {

  @Autowired private ReportService reports;
  @MockitoBean private ReportRepository repository;

  @Test
  @WithMockUser(roles = "USER")
  void userCannotPublish() {
    assertThatThrownBy(() -> reports.publish(42L))
        .isInstanceOf(AuthorizationDeniedException.class);
  }

  @Test
  @WithMockUser(roles = "EDITOR")
  void editorPublishes() {
    assertThatNoException().isThrownBy(() -> reports.publish(42L));
  }
}
```

`AuthorizationDeniedException` is what method security throws; catch that rather than `AccessDeniedException` in new code.

## Ownership and Tenant Boundaries

A filter-chain rule cannot answer "is this report Alice's?". That decision needs data:

```java
@SpringBootTest
@AutoConfigureMockMvc
class ReportAccessIntegrationTest extends AbstractIntegrationTest {

  @Test
  @WithMockUser(username = "bob", roles = "USER")
  void otherTenantUserCannotReadReport() {
    mvc.perform(get("/reports/{id}", aliceReportId))
       .andExpect(status().isNotFound());
  }
}
```

Choosing 404 instead of 403 for non-visible resources is a product decision about existence leakage; assert whichever the specification chose, and keep it stable across the slice and integration tests.

## Anti-Patterns

| Anti-Pattern | Fix |
| ------------ | --- |
| `addFilters = false` to make tests green | You stopped testing security; supply the principal instead |
| `SecurityContextHolder.getContext().setAuthentication(...)` in test code | Use `@WithMockUser`, `@WithUserDetails`, or a post-processor |
| `@WithMockUser(roles = "ROLE_ADMIN")` | Drop the prefix: `roles = "ADMIN"` |
| Asserting only `status().isForbidden()` | Also assert the failure is not a 401, and that no side effect happened |
| Testing `@PreAuthorize` with `@WebMvcTest` | Test method security on the service |
| `@WithMockUser` slice asserting `200` with no `403` case | `@Import` the chain and assert the rejection |
| `jwt()` against a chain without `oauth2ResourceServer` | Add the DSL and a mocked `JwtDecoder`, or drop the token post-processor |
| A test-only `SecurityFilterChain` that disables CSRF while production enables it | Test the chain that ships |
| Mocking `Authentication` deeply (`when(auth.getAuthorities())...`) | Build a real `TestingAuthenticationToken` / `Jwt` |
| Same 401 assertion in slice and integration tests | Keep it where the rule is implemented |

## Boot 3.x -> 4.x Notes

| Area | Boot 3.x | Boot 4.x |
| ---- | -------- | -------- |
| Security test dependency | `spring-security-test` added directly | `spring-boot-starter-security-test` |
| Mocking a bean in a slice | `@MockBean` | `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`) |
| `@WebMvcTest` package | `org.springframework.boot.test.autoconfigure.web.servlet` | `org.springframework.boot.webmvc.test.autoconfigure` |
| `@AutoConfigureMockMvc` package | `org.springframework.boot.test.autoconfigure.web.servlet` | `org.springframework.boot.webmvc.test.autoconfigure` |
| `MockMvcTester` assertions | `ResultMatcher` chains | `assertThat(result).hasStatus(...)` / `.matches(...)` |
| Custom chain inside slice | `@Import(SecurityConfig.class)` required | still required — `@Configuration` classes are outside the include filter |
| Reactive configurers | `SecurityMockServerConfigurers` | Unchanged |

## Links

- [Spring Security Testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html)
- [spring-security-test reference](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/request-post-processors.html)
- [Spring Security method security testing](https://docs.spring.io/spring-security/reference/servlet/test/method.html)
- [Spring Boot testing reference](https://docs.spring.io/spring-boot/reference/testing/)

## Related

- [mockmvc-tester.md](mockmvc-tester.md) - AssertJ-style MockMvc
- [webmvctest.md](webmvctest.md) - the MVC slice itself
- [websocket-testing.md](websocket-testing.md) - STOMP and `@EnableWebSocketSecurity`
- [test-slices-overview.md](test-slices-overview.md) - choosing a slice
