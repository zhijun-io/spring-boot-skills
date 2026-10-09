# MyBatis-Plus Testing

MyBatis-Plus (MP) replaces the JPA data layer, so `@DataJpaTest` and `TestEntityManager` do not apply. MP ships its own slice annotation, `@MybatisPlusTest`.

## Versions (verified against MP 3.5.17 on 2026-10-09)

- Spring Boot 4 requires `com.baomidou:mybatis-plus-spring-boot4-starter` — it exists since MP 3.5.13 and pulls mybatis-spring 4.0.0 (the pairing that makes Boot 4 work).
- Boot 3 uses `mybatis-plus-spring-boot3-starter`; `mybatis-plus-boot-starter` targets Boot 2. Booting Boot 4 with either older starter fails at context startup with a mybatis-spring version mismatch (typically `Invalid value type for attribute 'factoryBeanObjectType'`).
- Since 3.5.9, JSqlParser is decoupled: the pagination, tenant and other parsing inner interceptors need `com.baomidou:mybatis-plus-jsqlparser` on the classpath, or you get a `NoClassDefFoundError` on `net.sf.jsqlparser` when the interceptor loads.
- In the 3.5.x modular split (confirmed in 3.5.17), `IService`/`ServiceImpl` moved from `com.baomidou.mybatisplus.extension.service` to `com.baomidou.mybatisplus.spring.service` (artifact `mybatis-plus-spring`). Grep imports before blaming test changes.

## The @MybatisPlusTest Slice

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot4-starter-test</artifactId>
  <version>3.5.17</version>
  <scope>test</scope>
</dependency>
```

```java
@MybatisPlusTest
class OrderMapperTest {

  @Autowired
  private OrderMapper orderMapper;

  @Test
  void shouldFindPendingOrders() {
    orderMapper.insert(new Order("PENDING"));
    orderMapper.insert(new Order("DONE"));

    var pending = orderMapper.selectList(
        Wrappers.<Order>lambdaQuery().eq(Order::getStatus, "PENDING"));

    assertThat(pending).hasSize(1);
  }
}
```

What the slice does (from the 3.5.17 sources of `MybatisPlusTest` and its auto-configuration imports file):

- Loads only DataSource/tx/JdbcTemplate, Flyway/Liquibase, MyBatis and MP auto-configurations. Web layer, `@Service` and `@Component` beans are filtered out — default includes are empty, so import your own `@Configuration` with `@Import` or `includeFilters` when you need it.
- Registers `@Mapper`-annotated interfaces automatically via `MybatisPlusAutoConfiguration.AutoConfiguredMapperScannerRegistrar`, scanning the auto-configuration packages. An interface without `@Mapper` and without `@MapperScan` is not registered.
- `@Transactional` is baked in: every test rolls back, so no per-test cleanup is needed.
- `@AutoConfigureTestDatabase` is baked in with embedded replacement: a real configured DataSource is swapped for an in-memory one, which needs H2 (or similar) on the test classpath or the context fails to find a driver. Override per section below.
- Attributes: `properties()` for `key=value` overrides, `excludeAutoConfiguration()` to drop an auto-configuration.

## Real Database with Testcontainers

Same pattern as `@DataJpaTest` — disable the embedded swap:

```java
@MybatisPlusTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderMapperMySqlTest {

  @Container
  @ServiceConnection
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9");

  @Autowired
  private OrderMapper orderMapper;
}
```

Flyway and Liquibase are in the slice's auto-configuration list, so existing migrations create the schema; otherwise use `spring.sql.init` scripts. MySQL-dialect SQL (backtick identifiers, `ON DUPLICATE KEY`) does not run on the default embedded H2 — that is the point of this pattern.

## Testing Pagination

`selectPage` returns the full table (no `LIMIT`, `total` unset) unless `MybatisPlusInterceptor` with a `PaginationInnerInterceptor` is registered. The app's plugin config is an ordinary `@Configuration`, so the slice's type filter excludes it — bring it in explicitly:

```java
@MybatisPlusTest
@Import(MybatisPlusConfig.class)
class OrderMapperPageTest {

  @Autowired
  private OrderMapper orderMapper;

  @Test
  void shouldPageTenAtATime() {
    IntStream.range(0, 25).forEach(i -> orderMapper.insert(new Order("PENDING")));

    var page = orderMapper.selectPage(Page.of(1, 10),
        Wrappers.<Order>lambdaQuery().eq(Order::getStatus, "PENDING"));

    assertThat(page.getRecords()).hasSize(10);
    assertThat(page.getTotal()).isEqualTo(25);
  }
}
```

Alternative (verified in 3.5.17 bytecode): declare the inner interceptor as the bean and let `MybatisPlusInnerInterceptorAutoConfiguration` wrap it — it builds a `MybatisPlusInterceptor` from `InnerInterceptor` beans, `@ConditionalOnBean(InnerInterceptor)` and skipped when a `MybatisPlusInterceptor` bean already exists:

```java
@Bean
PaginationInnerInterceptor paginationInnerInterceptor() {
  return new PaginationInnerInterceptor(DbType.MYSQL); // match production dialect
}
```

`PaginationInnerInterceptor` requires the `mybatis-plus-jsqlparser` dependency.

## Service Layer: What Mocks and What Does Not

Single-record methods are default methods on `IRepository` that delegate straight to the mapper (`save` → `getBaseMapper().insert(entity)`), so plain Mockito works:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderMapper orderMapper;

  @InjectMocks
  private OrderService orderService; // extends ServiceImpl<OrderMapper, Order>

  @Test
  void shouldSaveViaMapper() {
    when(orderMapper.insert(any(Order.class))).thenReturn(1);

    assertThat(orderService.save(new Order("PENDING"))).isTrue();
  }
}
```

Batch methods (`saveBatch`, `updateBatchById`, `saveOrUpdateBatch`) do not go through the mapper: `CrudRepository.executeBatch` resolves `getSqlSessionFactory()` by unwrapping the mapper proxy's `SqlSession`, which a Mockito mock cannot supply — the call fails before touching SQL. Test them in `@MybatisPlusTest` against a real context, or move the logic under test above the batch call.

## Lambda Wrappers in Plain JUnit

`Wrappers.lambdaQuery()` resolves method references (`Order::getStatus`) against MP's cached `TableInfo` for the entity. Outside a Spring context nothing initializes that cache, so the first condition throws `can not find lambda cache for this entity [...]` (assertion in `AbstractLambdaWrapper`, 3.5.17).

For a wrapper built and consumed inside a slice test this never bites. If a unit test must build a lambda wrapper standalone, initialize the cache first:

```java
TableInfoHelper.initTableInfo(
    new MapperBuilderAssistant(new MybatisConfiguration(), ""), Order.class);
```

Otherwise test wrapper contents with string columns (`QueryWrapper.eq("status", ...)`) or move the assertion into the slice.

## Pitfalls

- `@MybatisPlusTest` finds no mappers: entity/mapper packages are outside the `@SpringBootApplication` root — add `@MapperScan` on a test config, since the auto scan only sees auto-configuration packages.
- The app's `MybatisPlusInterceptor`/`MetaObjectHandler`/optimistic-lock config silently absent in slice tests: the type filter excludes your `@Configuration` unless imported.
- Logical delete (`@TableLogic`): `deleteById` becomes an UPDATE and selects filter deleted rows — assert the flag changed, not that the row vanished.
- Auto-fill (`MetaObjectHandler`) only runs inside a real MyBatis session; mocked-mapper service tests never invoke it.
- Boot 4 with the Boot 2/3 starter fails at startup; pick the starter that matches the Boot major version.

Checked against: MyBatis-Plus 3.5.17 (jar bytecode and sources on Maven Central), Spring Boot 4.0.1 BOM, mybatis-spring 4.0.0.
