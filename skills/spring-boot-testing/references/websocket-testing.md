# WebSocket and STOMP Testing

Testing WebSocket endpoints and STOMP messaging in Spring Boot 4.x. Every API below was checked against `spring-websocket:7.0.9`, `spring-messaging:7.0.9` and `spring-security-messaging:7.1.1` on Boot 4.1.1.

## Pick the Right Layer

| What to prove | Where | Cost |
| ------------- | ----- | ---- |
| Handler logic — payload mapping, reply object, principal used as sender | Plain JUnit, `new ChatController()` | milliseconds |
| A service that pushes notifications through the broker | Plain JUnit + mocked `SimpMessagingTemplate` | milliseconds |
| Broker wiring, `/app` prefix, `/user` queue resolution, `Principal` propagation, real frame serialization | `@SpringBootTest(RANDOM_PORT)` + `WebSocketStompClient` | ~1-3 s |
| Message authorization and STOMP CSRF | Same, plus `@EnableWebSocketSecurity` | ~1-3 s |
| Handshake interception (session attributes, status codes) | Unit test the interceptor, or assert the failed handshake in a `RANDOM_PORT` test | milliseconds |

MockMvc never reaches a WebSocket upgrade: it fakes the servlet contract, not the protocol switch. Anything that depends on a real session must run over `RANDOM_PORT`.

## Dependencies

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
```

`spring-boot-starter-webmvc-test` brings `spring-boot-starter-test`, which is where `@SpringBootTest`, JUnit and AssertJ come from. For message security add `org.springframework.security:spring-security-messaging` explicitly — no Boot starter pulls it in, and `@EnableWebSocketSecurity` does not compile without it.

## Handler Logic Without Spring

`@MessageMapping` methods are ordinary public methods. Call them directly before writing any integration test:

```java
import java.security.Principal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ChatControllerTest {

  private final ChatController cut = new ChatController();
  private final Principal alice = () -> "alice";

  @Test
  void broadcastUsesAuthenticatedSender() {
    Outgoing outgoing = cut.broadcast(new Incoming("hi"), alice);
    assertThat(outgoing).isEqualTo(new Outgoing("alice", "hi"));
  }
}
```

Annotations are invisible to this test: `@SendTo` and `@SendToUser` are resolved by the broker, so assert routing in an integration test instead.

## STOMP Round-Trip Test

```java
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompIT {

  @LocalServerPort int port;
  WebSocketStompClient stompClient;

  @BeforeEach
  void setUp() {
    stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    stompClient.setDefaultHeartbeat(new long[] {0, 0});
  }

  @AfterEach
  void tearDown() {
    stompClient.stop();
  }

  @Test
  void broadcastReachesSubscriber() throws Exception {
    BlockingQueue<Outgoing> received = new LinkedBlockingQueue<>();
    StompSession session = connect("alice");

    session.subscribe("/topic/chat", new StompFrameHandler() {
      @Override public Type getPayloadType(StompHeaders headers) { return Outgoing.class; }
      @Override public void handleFrame(StompHeaders headers, Object payload) {
        received.offer((Outgoing) payload);
      }
    });

    session.send("/app/chat", new Incoming("hello"));

    Outgoing message = received.poll(5, TimeUnit.SECONDS);
    assertThat(message).isNotNull();
    assertThat(message.sender()).isEqualTo("alice");
    assertThat(message.content()).isEqualTo("hello");
    session.disconnect();
  }

  private StompSession connect(String user) throws Exception {
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Basic "
        + Base64.getEncoder().encodeToString((user + ":pw").getBytes(StandardCharsets.UTF_8)));
    return stompClient.connectAsync(URI.create("ws://localhost:" + port + "/ws"), headers,
        new StompHeaders(), new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
  }
}
```

Details that are easy to get wrong:

- **`JacksonJsonMessageConverter`, not `MappingJackson2MessageConverter`.** Boot 4 ships Jackson 3 (`tools.jackson.core:jackson-databind`) and no `com.fasterxml.jackson.databind.ObjectMapper`; the older converter throws `NoClassDefFoundError` when the first frame is encoded.
- **`stop()` in `@AfterEach`.** The client owns a task scheduler; leaving it running leaks a thread pool per test class.
- **`setDefaultHeartbeat(new long[] {0, 0})`.** Optional — the tests pass without it — but idle heartbeat traffic is log noise and a source of timing flakiness.
- **`get(timeout)` on `connectAsync`.** Never assert on a bare `CompletableFuture`; a hung CONNECT should fail the test, not hang the build.
- **`BlockingQueue.poll(timeout)` over `CountDownLatch.await`.** Both work; `poll` returns the payload, so the assertion is one line instead of a captured field.
- Timeouts bound the wait, they are not the thing under test. Keep them in seconds so a slow machine is not read as a bug.

### Authenticating the Session

`Principal` comes from the handshake, so credentials belong in the upgrade request, not in the CONNECT frame. Passing Basic in `WebSocketHttpHeaders` yields an authenticated session, which is what makes `Principal` parameters and `/user/queue/...` destinations work.

Per-user routing deserves an explicit assertion, because a wrong `setUserDestinationPrefix` delivers to the wrong inbox without failing anything:

```java
@Test
void sendToUserDeliversToOwnQueueOnly() throws Exception {
  BlockingQueue<Outgoing> aliceInbox = new LinkedBlockingQueue<>();
  StompSession alice = connect("alice");
  alice.subscribe("/user/queue/inbox", inbox(aliceInbox));

  alice.send("/app/dm", new Incoming("private"));
  assertThat(aliceInbox.poll(5, TimeUnit.SECONDS).content()).isEqualTo("private");

  BlockingQueue<Outgoing> bobInbox = new LinkedBlockingQueue<>();
  StompSession bob = connect("bob");
  bob.subscribe("/user/queue/inbox", inbox(bobInbox));
  assertThat(bobInbox.poll(1, TimeUnit.SECONDS)).isNull();
}

private StompFrameHandler inbox(BlockingQueue<Outgoing> target) {
  return new StompFrameHandler() {
    @Override public Type getPayloadType(StompHeaders headers) { return Outgoing.class; }
    @Override public void handleFrame(StompHeaders headers, Object payload) {
      target.offer((Outgoing) payload);
    }
  };
}
```

### Asserting a Rejected Handshake

The client exception is the observable behaviour of a protected endpoint. With `/ws` behind `.authenticated()`, an unauthenticated handshake fails during the upgrade:

```java
import static org.assertj.core.api.Assertions.catchThrowable;

Throwable thrown = catchThrowable(() -> stompClient
    .connectAsync(URI.create("ws://localhost:" + port + "/ws"), new WebSocketHttpHeaders(),
        new StompHeaders(), new StompSessionHandlerAdapter() {})
    .get(5, TimeUnit.SECONDS));

assertThat(thrown).hasCauseInstanceOf(jakarta.websocket.DeploymentException.class);
```

`DeploymentException` means "the upgrade did not return 101". It does not distinguish 401 from 404 — read the server log, or point a plain HTTP client at the URL when the status itself is the contract.

## STOMP with Spring Security

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
class MessageSecurityConfig {

  @Bean
  AuthorizationManager<Message<?>> messageAuthorizationManager(
      MessageMatcherDelegatingAuthorizationManager.Builder messages) {
    messages.simpSubscribeDestMatchers("/topic/**").hasRole("USER")
        .simpDestMatchers("/app/**").hasRole("USER")
        .anyMessage().authenticated();
    return messages.build();
  }
}
```

`@EnableWebSocketSecurity` installs `SecurityContextChannelInterceptor` (reads the principal from `SimpMessageHeaderAccessor.USER_HEADER`), `AuthorizationChannelInterceptor` and `XorCsrfChannelInterceptor` on the client inbound channel. Keep it in a `@Configuration` that production also loads — a rule that only exists in the test proves nothing.

### CSRF Is Enforced on CONNECT

`XorCsrfChannelInterceptor` checks every `CONNECT` frame, and `http.csrf(customizer -> customizer.disable())` does not remove it: disabling the servlet filter only removes the token the interceptor compares against. Observed with `@EnableWebSocketSecurity` active:

| Servlet CSRF | CONNECT header | Result |
| ------------ | -------------- | ------ |
| `csrf().disable()` | none | connection closed, `MissingCsrfTokenException` server-side |
| enabled | none | connection closed |
| enabled | `X-XSRF-TOKEN: <encoded>` | CONNECT accepted |

The workable client recipe mirrors a browser:

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

record Csrf(String headerName, String encoded, String rawCookie) {}

private static String textBetween(String json, String field) {
  String key = "\"" + field + "\":\"";
  int start = json.indexOf(key) + key.length();
  return json.substring(start, json.indexOf('"', start));
}

private Csrf fetchCsrfToken() throws Exception {
  HttpResponse<String> response = HttpClient.newHttpClient().send(
      HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/csrf")).GET().build(),
      HttpResponse.BodyHandlers.ofString());

  String raw = response.headers().allValues("set-cookie").stream()
      .filter(c -> c.startsWith("XSRF-TOKEN="))
      .map(c -> c.substring("XSRF-TOKEN=".length(), c.indexOf(';')))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("no XSRF-TOKEN cookie in " + response.headers()));

  String body = response.body();
  return new Csrf(textBetween(body, "headerName"), textBetween(body, "token"), raw);
}

@Test
void connectWithCsrfToken() throws Exception {
  Csrf csrf = fetchCsrfToken();

  WebSocketHttpHeaders handshake = new WebSocketHttpHeaders();
  handshake.add(HttpHeaders.AUTHORIZATION, "Basic "
      + Base64.getEncoder().encodeToString("alice:pw".getBytes(StandardCharsets.UTF_8)));
  handshake.add(HttpHeaders.COOKIE, "XSRF-TOKEN=" + csrf.rawCookie());

  StompHeaders connectHeaders = new StompHeaders();
  connectHeaders.add(csrf.headerName(), csrf.encoded());

  StompSession session = stompClient.connectAsync(URI.create("ws://localhost:" + port + "/ws"),
      handshake, connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

  assertThat(session.isConnected()).isTrue();
  session.disconnect();
}
```

`textBetween` is deliberate: the shape of the `/csrf` response is a test fixture, not production API. A `tools.jackson` read or a JsonPath expression works just as well — keep the extraction visible at the point where the two token forms are separated.

The two values are not interchangeable. The cookie holds the raw token, which `CsrfTokenHandshakeInterceptor` copies into the session as the expected value; the header must hold the XOR-encoded form, which `XorCsrfChannelInterceptor` decodes before comparing. Sending the raw token as the header fails. `CookieCsrfTokenRepository.withHttpOnlyFalse()` also renames the header to `X-XSRF-TOKEN`, so read `CsrfToken.getHeaderName()` instead of hard-coding it.

An endpoint that renders the token is production code a SPA backend usually already exposes:

```java
@GetMapping("/csrf")
Map<String, String> csrf(CsrfToken csrf) {
  return Map.of("headerName", csrf.getHeaderName(), "token", csrf.getToken());
}
```

### Denied Messages Close the Session

Authorization failures after CONNECT do not produce a frame the client can read. A `SEND` to `/app/chat` by a user without `ROLE_USER` raised `AccessDeniedException` in `AuthorizationChannelInterceptor.preSend`, the server closed the session, and `StompSessionHandler.handleException` was never invoked:

```java
carol.send("/app/chat", new Incoming("nope"));

assertThat(carol.isConnected()).isFalse();
```

Assert `isConnected()` — and that no subscriber received the message — instead of hunting for an ERROR frame. `handleException(StompSession, StompCommand, StompHeaders, byte[], Throwable)` is still the right hook for frames the broker rejects on a live session, so override it when you need the failing command and payload.

## Destination Annotations

| Annotation | Package |
| ---------- | ------- |
| `@MessageMapping` | `org.springframework.messaging.handler.annotation` |
| `@SendTo` | `org.springframework.messaging.handler.annotation` |
| `@SendToUser` | `org.springframework.messaging.simp.annotation` |

`@SendTo` is not in `org.springframework.messaging.simp.annotation`; an auto-import suggested by a neighbouring `@SendToUser` compiles to nothing and the broadcast silently stops.

## Boot 3.x -> 4.x Notes

| Area | Boot 3.x | Boot 4.x |
| ---- | -------- | -------- |
| STOMP JSON converter | `MappingJackson2MessageConverter` | `JacksonJsonMessageConverter` (Jackson 3 only) |
| WebSocket security DSL | `AbstractSecurityWebSocketMessageBrokerConfigurer` | removed in Security 7 — use `@EnableWebSocketSecurity` + an `AuthorizationManager<Message<?>>` bean |
| Message rules | `SimpMessageMappingIntrospector` + `.messageMatchers(...)` | `MessageMatcherDelegatingAuthorizationManager.Builder` |
| `@LocalServerPort` | `org.springframework.boot.test.web.server` | unchanged |
| Security test dependency | `spring-security-test` | `spring-boot-starter-security-test` |

## Anti-Patterns

| Anti-Pattern | Fix |
| ------------ | --- |
| Testing `@MessageMapping` routing with MockMvc | MockMvc cannot upgrade; call the handler or use `RANDOM_PORT` |
| `Thread.sleep(...)` waiting for a frame | `BlockingQueue.poll(timeout, unit)` |
| Hard-coded port in tests | `RANDOM_PORT` + `@LocalServerPort` |
| One `WebSocketStompClient` shared across test classes | Create per test class, `stop()` after each |
| Asserting only "no exception thrown" | Assert the payload that arrived and that the wrong inbox stayed empty |
| Disabling CSRF to make a STOMP test pass | With `@EnableWebSocketSecurity` it stays enforced; supply the token |
| Message rules declared only in test configuration | Put them in the configuration production loads |

## Links

- [Spring Boot — WebSocket testing](https://docs.spring.io/spring-boot/reference/testing/)
- [Spring Framework — STOMP messaging](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)
- [Spring Security — WebSocket authorization](https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html)
- [`WebSocketStompClient` API](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/client.html)

## Related

- [security-testing.md](security-testing.md) — servlet authentication and `@WithMockUser`
- [test-slices-overview.md](test-slices-overview.md) — choosing a slice
- [maven-plugin.md](maven-plugin.md) — Surefire/Failsafe naming for `*IT` tests
