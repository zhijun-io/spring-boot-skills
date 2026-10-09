---
name: spring-boot-testing
description: >-
  Selects and implements the right Spring Boot 4 test layer with JUnit 6, AssertJ and Mockito: plain unit tests,
  @WebMvcTest, @DataJpaTest, @JsonTest, @RestClientTest, @SpringBootTest, Testcontainers, Maven Surefire/Failsafe
  setup, Spring Security test support (@WithMockUser, @MockitoBean JwtDecoder, SecurityFilterChain, jwt(),
  oauth2ResourceServer, method security), and WebSocket/STOMP testing (WebSocketStompClient, @MessageMapping,
  @SendToUser, @EnableWebSocketSecurity), and MyBatis-Plus persistence testing (@MybatisPlusTest, BaseMapper,
  IService/ServiceImpl, LambdaQueryWrapper, PaginationInnerInterceptor). Use when writing or fixing tests, choosing
  between MockMvc, MockMvcTester, RestTestClient, getting "No qualifying bean of type 'MockMvc'", "@MockBean is
  deprecated", "can not find lambda cache for this entity", slice tests returning 200 where 403 was expected,
  flaky @SpringBootTest contexts, or slow test suites.
---

# Spring Boot Testing

This skill provides expert guide for testing Spring Boot 4 applications with modern patterns and best practices.

## Tested With

Spring Boot 4.1.1, Spring Framework 7.0.9, Spring Security 7.1.1, JUnit 6.0.3, AssertJ 3.27, Mockito (managed by the Boot BOM), Testcontainers 2.0, Maven Surefire/Failsafe 3.5, MyBatis-Plus 3.5.17. Facts in the references were verified against these versions; Boot 3.x differences are called out per reference.

## Do NOT Use This Skill When

- The code under test is not Spring: plain JUnit + AssertJ advice still applies, but the slice tables here do not.
- The task is end-to-end UI or browser testing.
- The project needs a load or soak test; these references cover correctness, not throughput.

## Core Principles

1. **Test Pyramid**: Unit (fast) > Slice (focused) > Integration (complete)
2. **Right Tool**: Use the narrowest slice that gives you confidence
3. **AssertJ Style**: Fluent, readable assertions over verbose matchers
4. **Modern APIs**: Prefer MockMvcTester and RestTestClient over legacy alternatives

## Which Test Slice?

| Scenario | Annotation | Reference |
|----------|------------|-----------|
| Controller + HTTP semantics | `@WebMvcTest` | [references/webmvctest.md](references/webmvctest.md) |
| Repository + JPA queries | `@DataJpaTest` | [references/datajpatest.md](references/datajpatest.md) |
| Mapper + SQL with MyBatis-Plus | `@MybatisPlusTest` | [references/mybatis-plus-testing.md](references/mybatis-plus-testing.md) |
| REST client + external APIs | `@RestClientTest` | [references/restclienttest.md](references/restclienttest.md) |
| JSON (de)serialization | `@JsonTest` | [references/test-slices-overview.md](references/test-slices-overview.md) |
| URL rules, 401 vs 403, CSRF, JWT/principal | `@WebMvcTest` + `@Import(SecurityConfig.class)` | [references/security-testing.md](references/security-testing.md) |
| `@PreAuthorize` on a service | `@SpringBootTest` with a minimal context | [references/security-testing.md](references/security-testing.md) |
| WebSocket handshake / STOMP round-trip | `@SpringBootTest(RANDOM_PORT)` + `WebSocketStompClient` | [references/websocket-testing.md](references/websocket-testing.md) |
| Full application | `@SpringBootTest` | [references/test-slices-overview.md](references/test-slices-overview.md) |

## Test Slices Reference

- [references/test-slices-overview.md](references/test-slices-overview.md) - Decision matrix and comparison
- [references/webmvctest.md](references/webmvctest.md) - Web layer with MockMvc
- [references/datajpatest.md](references/datajpatest.md) - Data layer with Testcontainers
- [references/restclienttest.md](references/restclienttest.md) - REST client testing
- [references/security-testing.md](references/security-testing.md) - Authentication, authorization, method security
- [references/websocket-testing.md](references/websocket-testing.md) - WebSocket handshake and STOMP round-trips
- [references/mybatis-plus-testing.md](references/mybatis-plus-testing.md) - MyBatis-Plus slice, pagination, service layer

## Testing Tools Reference

- [references/mockmvc-tester.md](references/mockmvc-tester.md) - AssertJ-style MockMvc (3.2+)
- [references/mockmvc-classic.md](references/mockmvc-classic.md) - Traditional MockMvc (pre-3.2)
- [references/resttestclient.md](references/resttestclient.md) - Spring Boot 4+ REST client
- [references/mockitobean.md](references/mockitobean.md) - Mocking dependencies

## Assertion Libraries

- [references/assertj-basics.md](references/assertj-basics.md) - Scalars, strings, booleans, dates
- [references/assertj-collections.md](references/assertj-collections.md) - Lists, Sets, Maps, arrays

## Testcontainers

- [references/testcontainers-jdbc.md](references/testcontainers-jdbc.md) - PostgreSQL, MySQL, etc.

## Test Data Generation

- [references/instancio.md](references/instancio.md) - Generate complex test objects (3+ properties)

## Performance & Migration

- [references/context-caching.md](references/context-caching.md) - Speed up test suites
- [references/sb4-migration.md](references/sb4-migration.md) - Spring Boot 4.0 changes

## Build Configuration

- [references/maven-plugin.md](references/maven-plugin.md) - Surefire/Failsafe split, test commands, JUnit parallelism, tree output, JaCoCo

## Quick Decision Tree

```
Testing a controller endpoint?
  Yes → @WebMvcTest with MockMvcTester

Testing repository queries?
  Yes → @DataJpaTest with Testcontainers (real DB)

Testing MyBatis-Plus mappers or IService?
  Yes → @MybatisPlusTest (mybatis-plus-spring-boot4-starter-test)

Testing business logic in service?
  Yes → Plain JUnit + Mockito (no Spring context)

Testing external API client?
  Yes → @RestClientTest with MockRestServiceServer

Testing JSON mapping?
  Yes → @JsonTest

Testing who may call an endpoint?
  Yes → @WebMvcTest, import the real SecurityFilterChain, assert 200 and 403
  Method security (@PreAuthorize) → @SpringBootTest with the service + config only

Testing a WebSocket/STOMP endpoint?
  Handler logic only → plain JUnit, call the @MessageMapping method
  Broker, Principal, @SendToUser → @SpringBootTest(RANDOM_PORT) + WebSocketStompClient

Need full integration test?
  Yes → @SpringBootTest with minimal context config
```

## Spring Boot 4 Highlights

- **RestTestClient**: Modern alternative to TestRestTemplate
- **@MockitoBean**: Replaces @MockBean (deprecated)
- **MockMvcTester**: AssertJ-style assertions for web tests
- **Modular starters**: Technology-specific test starters
- **Context pausing**: Automatic pausing of cached contexts (Spring Framework 7)

## Testing Best Practices

### Code Complexity Assessment

When a method or class is too complex to test effectively:

1. **Analyze complexity** - If you need more than 5-7 test cases to cover a single method, it's likely too complex
2. **Recommend refactoring** - Suggest breaking the code into smaller, focused functions
3. **User decision** - If the user agrees to refactor, help identify extraction points
4. **Proceed if needed** - If the user decides to continue with the complex code, implement tests despite the difficulty

**Example of refactoring recommendation:**
```java
// Before: Complex method hard to test
public Order processOrder(OrderRequest request) {
  // Validation, discount calculation, payment, inventory, notification...
  // 50+ lines of mixed concerns
}

// After: Refactored into testable units
public Order processOrder(OrderRequest request) {
  validateOrder(request);
  var order = createOrder(request);
  applyDiscount(order);
  processPayment(order);
  updateInventory(order);
  sendNotification(order);
  return order;
}
```

### Avoid Code Redundancy

Create helper methods for commonly used objects and mock setup to enhance readability and maintainability.

### Test Organization with @DisplayName

Use descriptive display names to clarify test intent:

```java
@Test
@DisplayName("Should calculate discount for VIP customer")
void shouldCalculateDiscountForVip() { }

@Test
@DisplayName("Should reject order when customer has insufficient credit")
void shouldRejectOrderForInsufficientCredit() { }
```

### Test Coverage Order

Always structure tests in this order:

1. **Main scenario** - The happy path, most common use case
2. **Other paths** - Alternative valid scenarios, edge cases
3. **Exceptions/Errors** - Invalid inputs, error conditions, failure modes

### Test Production Scenarios

Write tests with real production scenarios in mind. This makes tests more relatable and helps understand code behavior in actual production cases.

### Test Coverage Goals

Aim for 80% line coverage as a practical balance between quality and effort. Higher coverage is beneficial but not the only goal. Treat branch coverage on security rules and failure paths as the number that matters; a green percentage built on `200`-only assertions is not evidence of authorization.

Use the JaCoCo Maven plugin for coverage reporting and tracking - see [references/maven-plugin.md](references/maven-plugin.md) for the configuration and its pitfalls.


**Coverage Rules:**
- 80+% coverage minimum
- Focus on meaningful assertions, not just execution

**What to Prioritize:**
1. Business-critical paths (payment processing, order validation)
2. Complex algorithms (pricing, discount calculations)
3. Error handling (exceptions, edge cases)
4. Integration points (external APIs, databases)

## Dependencies (Spring Boot 4)

Unit tests and integration tests must not share one execution: configure Surefire and Failsafe as described in [references/maven-plugin.md](references/maven-plugin.md) before writing the first `@SpringBootTest` that needs Docker.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- For WebMvc tests -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- For Spring Security tests (pulls spring-security-test) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- For Testcontainers -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
```

## Links

- https://github.com/spring-ai-community/spring-testing-skills
- https://github.com/github/awesome-copilot/tree/main/skills/spring-boot-testing
