# rules_micronaut

Bazel rules for building, running and testing Java [Micronaut](https://micronaut.io)
applications. They run Micronaut's annotation processors, put each Micronaut
feature's dependencies behind one label, and run JUnit tests, including
`@MicronautTest`.

Requires Bazel 8.4 or newer, Micronaut 5 and JDK 25, on Linux or macOS. Windows
is unvalidated. Java is the only supported source language.

Start with [hello-world](examples/hello-world) for an HTTP application with
PostgreSQL and JPA CRUD endpoints.

## Setup

Micronaut artifacts come from your own `rules_jvm_external` resolution, with
versions selected by the Micronaut platform BOM. This `MODULE.bazel` covers an
HTTP application with JSON serialization, YAML configuration, Logback and JUnit
tests:

```starlark
bazel_dep(name = "rules_micronaut", version = "0.1.0")
bazel_dep(name = "rules_java", version = "9.3.0")
bazel_dep(name = "rules_jvm_external", version = "7.2")

maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
maven.install(
    artifacts = [
        # Core annotation processing
        "io.micronaut:micronaut-inject-java",
        # HTTP server and client
        "io.micronaut:micronaut-http-validation",
        "io.micronaut:micronaut-http-server-netty",
        "io.micronaut:micronaut-http-client",
        # JSON serialization
        "io.micronaut.serde:micronaut-serde-processor",
        "io.micronaut.serde:micronaut-serde-jackson",
        # YAML configuration and logging
        "org.yaml:snakeyaml",
        "ch.qos.logback:logback-classic",
        # JUnit
        "io.micronaut.test:micronaut-test-junit5",
        "org.junit.jupiter:junit-jupiter-engine",
        "org.junit.platform:junit-platform-reporting",
    ],
    boms = ["io.micronaut.platform:micronaut-platform:5.2.5"],
    lock_file = "//:maven_install.json",
    repositories = ["https://repo.maven.apache.org/maven2"],
    resolver = "maven",
)
use_repo(maven, "maven")

micronaut = use_extension("@rules_micronaut//micronaut:extensions.bzl", "micronaut")
micronaut.maven(repo = "@maven")
use_repo(micronaut, "micronaut")
```

Select JDK 25 in `.bazelrc`:

```text
common --java_language_version=25 --java_runtime_version=remotejdk_25
common --tool_java_language_version=25 --tool_java_runtime_version=remotejdk_25
```

Create the Maven lockfile, and commit it together with `MODULE.bazel.lock`:

```sh
touch maven_install.json
REPIN=1 bazel run @maven//:pin
```

Rerun the pin command whenever you change the artifacts or the BOM.

## Build, run and test

With sources under `src/main/java` and tests under `src/test/java`:

```starlark
load("@rules_micronaut//micronaut:defs.bzl", "micronaut_application", "micronaut_library", "micronaut_test")

micronaut_library(
    name = "lib",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"], allow_empty = True),
    deps = ["@micronaut//:http", "@micronaut//:serialization"],
)

micronaut_application(
    name = "app",
    main_class = "com.example.Application",
    runtime_deps = [
        ":lib",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:org_yaml_snakeyaml",
        "@micronaut//:serialization_jackson",
    ],
)

micronaut_test(
    name = "HelloControllerTest",
    srcs = ["src/test/java/com/example/HelloControllerTest.java"],
    runtime_deps = [
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:io_micronaut_micronaut_http_client",
        "@maven//:org_yaml_snakeyaml",
        "@micronaut//:serialization_jackson",
    ],
    deps = [
        ":lib",
        "@micronaut//:http_client",
    ],
)
```

```sh
bazel run //:app
bazel test //...
bazel build //:app_deploy.jar
```

- `micronaut_library` compiles sources with Micronaut's processors.
- `micronaut_application` builds a runnable binary and a deploy jar. `main_class`
  calls `Micronaut.run(...)`. It can also compile `srcs` directly.
- `micronaut_test` runs one JUnit test class. `test_class` is inferred from the
  source whose name matches the target under `src/test/java`, `src/main/java`,
  `java` or `javatests`; set it explicitly otherwise.

Attribute documentation is published with each release on the
[Bazel Central Registry](https://registry.bazel.build/modules/rules_micronaut).

## Features

Add a feature to the `deps` of each target that compiles sources using it. A
feature supplies its APIs, annotation processors and runtime dependencies;
`core` is always added. Processors follow `exported_plugins` semantics: they run
on direct dependants only, so a feature can be re-exported through a
`java_library(exports = [...])` bundle but not picked up transitively.

Each feature is available as `@micronaut//:<name>` and links to an example:

| Feature                                                                                     | Provides                                                   |
| ------------------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| [`cache`](e2e/smoke/features/cache)                                                         | Cache annotations and APIs                                 |
| [`core`](e2e/smoke/features/core)                                                           | Dependency injection, configuration and AOP                |
| [`data_azure_cosmos`](e2e/smoke/features/data-azure-cosmos)                                 | Azure Cosmos DB repositories                               |
| [`data_hibernate_reactive`](e2e/smoke/features/data-hibernate-reactive)                     | Hibernate Reactive repositories                            |
| [`data_jdbc`](e2e/smoke/features/data-jdbc)                                                 | JDBC repositories                                          |
| [`data_jpa`](e2e/smoke/features/data-jpa)                                                   | Hibernate JPA repositories                                 |
| [`data_jpa_metamodel`](e2e/smoke/features/data-jpa-metamodel)                               | Micronaut Data's JPA static metamodel                      |
| [`data_mongodb`](e2e/smoke/features/data-mongodb)                                           | MongoDB repositories                                       |
| [`data_r2dbc`](e2e/smoke/features/data-r2dbc)                                               | R2DBC repositories                                         |
| [`data_spring_jdbc`](e2e/smoke/features/data-spring-jdbc)                                   | Spring JDBC integration; includes `data_jdbc`              |
| [`eclipsestore`](e2e/smoke/features/eclipsestore)                                           | EclipseStore integration                                   |
| [`guice`](e2e/smoke/features/guice)                                                         | Guice module bindings imported into Micronaut              |
| [`hibernate_jpamodelgen`](e2e/smoke/features/hibernate-jpamodelgen)                         | Hibernate's JPA static metamodel                           |
| [`http`](e2e/smoke/features/http)                                                           | HTTP APIs and compile-time route validation                |
| [`http_client`](e2e/smoke/features/http-client)                                             | HTTP client APIs and declarative clients; includes `http`  |
| [`jakarta_el`](e2e/smoke/features/jakarta-el)                                               | Jakarta Expression Language                                |
| [`jax_rs`](e2e/smoke/features/jax-rs)                                                       | Jakarta REST annotations; includes `http`                  |
| [`json_schema`](e2e/smoke/features/json-schema)                                             | JSON Schema generation, under `META-INF/schemas`           |
| [`langchain4j`](e2e/smoke/features/langchain4j)                                             | LangChain4j AI services                                    |
| [`lombok`](e2e/smoke/features/lombok)                                                       | Compile-only Lombok annotations; explicit plugin required  |
| [`micrometer_annotation`](e2e/smoke/features/micrometer-annotation)                         | Micrometer `@Counted` and `@Timed`                         |
| [`multi_tenancy`](e2e/smoke/features/multi-tenancy)                                         | Tenant resolution                                          |
| [`openapi`](e2e/smoke/features/openapi)                                                     | OpenAPI specification generation, under `META-INF/swagger` |
| [`openapi_adoc`](e2e/smoke/features/openapi-adoc)                                           | OpenAPI and AsciiDoc generation; includes `openapi`        |
| [`opendi`](e2e/smoke/features/opendi)                                                       | OpenDI CDI Lite                                            |
| [`picocli`](e2e/smoke/features/picocli)                                                     | Picocli commands and native-image metadata                 |
| [`security`](e2e/smoke/features/security)                                                   | Micronaut Security                                         |
| [`serialization`](e2e/smoke/features/serialization)                                         | Serialization APIs and code generation, without a mapper   |
| [`serialization_bson`](e2e/smoke/features/serialization-bson)                               | BSON mapper; includes `serialization`                      |
| [`serialization_jackson`](e2e/smoke/features/serialization-jackson)                         | Jackson mapper; includes `serialization`                   |
| [`serialization_jsonp`](e2e/smoke/features/serialization-jsonp)                             | JSON-P mapper; includes `serialization`                    |
| [`servlet`](e2e/smoke/features/servlet)                                                     | Jakarta Servlet APIs and processing                        |
| [`sourcegen_generator`](e2e/smoke/features/sourcegen-generator)                             | Source generators for annotations such as `@Builder`       |
| [`spring`](e2e/smoke/features/spring)                                                       | Spring bean and injection annotations                      |
| [`spring_boot`](e2e/smoke/features/spring-boot)                                             | Spring Boot annotations; includes `spring`                 |
| [`spring_web`](e2e/smoke/features/spring-web)                                               | Spring Web routes; includes `spring`                       |
| [`tracing_opentelemetry_annotations`](e2e/smoke/features/tracing-opentelemetry-annotations) | OpenTelemetry span annotations                             |
| [`validation`](e2e/smoke/features/validation)                                               | Bean validation                                            |

Your Maven resolution must contain the artifacts of every feature you select,
including the features it includes. [features.bzl](micronaut/private/features.bzl)
lists them per feature and per server. Artifacts can be declared directly in
`maven.install` or resolved transitively, as in the setup above. With
`strict_visibility = True`, declare all selected features' artifacts explicitly.

A few artifacts are not managed by the platform BOM and need an explicit version;
the [example module](e2e/smoke/MODULE.bazel) has working versions for all of them.

Features supply code generation and APIs, not interchangeable implementations.
Choose those in the `runtime_deps` of the application or test:

- **Serialization:** compile with `serialization`, then run with
  `serialization_jackson`, `serialization_bson` or `serialization_jsonp`.
- **YAML configuration:** `@maven//:org_yaml_snakeyaml`.
- **Logging:** a backend such as `@maven//:ch_qos_logback_logback_classic`.
- **Databases:** a connection pool and driver, such as
  `@maven//:io_micronaut_sql_micronaut_jdbc_hikari` and `@maven//:com_h2database_h2`.
- **Caching:** a backend such as `@maven//:io_micronaut_cache_micronaut_cache_caffeine`.

HTTP clients, including declarative `@Client` interfaces, use
`@micronaut//:http_client` in `deps`. This includes `http` and exports the client
API without selecting a transport. Add an implementation, such as
`@maven//:io_micronaut_micronaut_http_client` for Netty, to the application or
test's `runtime_deps`. The implementation must be present in your Maven resolution.

## Servers

Applications and tests select a server with `runtime`: `"netty"` (the default),
`"jetty"`, `"tomcat"`, `"undertow"` or `"none"`. The server's artifact must be
present in your Maven resolution.

Use `runtime = "none"` with `@MicronautTest(startApplication = false)` for tests
that only need beans. For a servlet server, also add `@micronaut//:servlet` to
the `deps` of targets compiling servlet sources.

## Feature notes

- **`data_hibernate_reactive`:** set `jpa.default.reactive=true`, add a Vert.x
  SQL client, and add `@maven//:io_projectreactor_reactor_core` to `deps` when
  using Reactor types.
- **`data_spring_jdbc`:** set `datasources.default.transaction-manager=springJdbc`.
- **`data_jpa_metamodel` and `hibernate_jpamodelgen`:** choose one.
- **`openapi`:** to document controllers compiled in other libraries, add them
  to `deps` and name them with `@OpenAPIInclude`.
- **`openapi_adoc`:** add `javacopts = ["-Amicronaut.openapi.adoc.enabled=true"]`.
- **`serialization_jsonp`:** `micronaut-serde-jsonp` depends on Parsson. To use
  another provider, declare the artifact like this instead of listing it in
  `artifacts`, and add your provider to `maven.install` and `runtime_deps`:

  ```starlark
  maven.artifact(
      artifact = "micronaut-serde-jsonp",
      exclusions = ["org.eclipse.parsson:parsson"],
      group = "io.micronaut.serde",
  )
  ```

### Lombok

Add `"org.projectlombok:lombok"` to your `maven.install` artifacts and repin.
The platform BOM selects its version.

[Lombok must run before Micronaut's processors](https://docs.micronaut.io/latest/guide/#lombok).
Adding `@micronaut//:lombok` to `deps` supplies compile-only Lombok annotations;
it does not enable Lombok processing. List the generated
`@micronaut//:lombok_processor` first in `plugins` on every library compiling
Lombok sources:

```starlark
load("@rules_micronaut//micronaut:defs.bzl", "micronaut_library")

micronaut_library(
    name = "models",
    srcs = ["Message.java"],
    export_full_jar = True,
    plugins = ["@micronaut//:lombok_processor"],
    deps = [
        "@micronaut//:lombok",
        "@micronaut//:serialization",
    ],
)
```

Explicit plugins run before processors exported by dependencies. The same
`plugins` setup applies to plain `java_library`, and to applications or tests
that compile Lombok sources directly.

Omitting the plugin can leave annotations unprocessed without a compile error
until code references generated members.

Lombok modifies javac's syntax tree and does not run in Turbine header
compilation. Set `export_full_jar = True` so consumers can use its generated getters,
setters, constructors and builders through `deps = [":models"]`. This makes
the compiled members available without running Lombok in the header compiler;
the processor's `generates_api` remains `False`. Lombok itself is not added to
the runtime classpath. See the
[Lombok smoke test](e2e/smoke/features/lombok) for a complete example exercising
generated members, Micronaut introspection and JSON serialization.

## Header jars

Targets compile against the header jars of their dependencies, as with any Java
rule, so changing a dependency's implementation does not recompile its
dependants. `MICRONAUT_JAVACOPTS` retains private fields that Micronaut reads
from dependencies. Features that expose generated types also run their
processors in header compilation; other processors run only during normal
compilation.

A header jar still omits private methods, so Micronaut would never call a
private `@Inject`, `@PostConstruct` or `@PreDestroy` method for a subclass in
another target. The build therefore rejects such methods in non-final classes.
Make the method package-private or protected, or make the class final. To
report warnings instead while migrating, build with
`--javacopt=-Arules_micronaut.private_injection=warn`.

A header jar also omits types and members generated by processors that do not
run in header compilation, including Hibernate's metamodel generator and Lombok.
Set `export_full_jar = True` on a `micronaut_library` when consumers need those members,
or a private method you have kept with `@SuppressWarnings("MicronautPrivateInjection")`:

```starlark
micronaut_library(
    name = "entities",
    srcs = ["Entity.java"],
    export_full_jar = True,
    deps = ["@micronaut//:hibernate_jpamodelgen"],
)

micronaut_library(
    name = "queries",
    srcs = ["Queries.java"],
    deps = [":entities"],
)
```

The flag defaults to `False`. With `True`, consumers compile against the full
jar, including exported libraries, through the normal target label. Runtime
behavior is unchanged. Consumers must wait for full compilation, and changes
to implementation code can recompile dependants. This has the same build cost
as using an explicit `micronaut_full_jar_deps` wrapper.

For a library you cannot change, or to opt in only for particular consumers,
use `micronaut_full_jar_deps` instead:

```starlark
load("@rules_micronaut//micronaut:defs.bzl", "micronaut_full_jar_deps", "micronaut_library")

micronaut_full_jar_deps(
    name = "entities_full",
    deps = [":entities"],
)

micronaut_library(
    name = "queries",
    srcs = ["Queries.java"],
    deps = [":entities_full"],
)
```

Dependants of `queries` also compile against the full jar and are recompiled
whenever `entities` changes, so wrap only what needs it.

## Custom Java rules and processors

To process Micronaut sources with plain Java rules, add a feature to `deps` and
`MICRONAUT_JAVACOPTS` to `javacopts`:

```starlark
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_micronaut//micronaut:defs.bzl", "MICRONAUT_JAVACOPTS")

java_library(
    name = "models",
    srcs = ["Greeting.java"],
    javacopts = MICRONAUT_JAVACOPTS,
    deps = ["@micronaut//:serialization"],
)
```

Pass extra `java_plugin` targets through `plugins`. Set `processor_class` for
standalone annotation processors. Micronaut visitors used only during normal
compilation can omit it: core discovers them on the processor path.

For a visitor whose generated types are used by other targets, set
`generates_api = True`, supply the Micronaut processor entry point that
dispatches it, and include `micronaut-inject-java` in the plugin's `deps`.
The [SourceGen example](e2e/smoke/features/core/base/BUILD.bazel) uses
`TypeElementVisitorProcessor`. A visitor without an entry point cannot run
by itself during header compilation.

## Rule behaviour

- The public macros are legacy wrappers that forward standard Java attributes
  to `rules_java` and `contrib_rules_jvm`.
- `deps`, `runtime_deps`, `resources` and compiler and JVM flags accept
  `select()`. `srcs`, `runtime`, `test_class` and `export_full_jar` do not.
- Without `srcs`, a library only carries `resources` and `exports`, and an
  application launches the libraries in its `runtime_deps`.
- Tests use the JUnit Platform runner from
  [contrib_rules_jvm](https://github.com/bazel-contrib/rules_jvm), with tag and
  engine selection, sharding, JUnit XML reports and `--test_filter`.
- Tests preserve caller-supplied tags. Use `tags = ["no-lint"]` to opt out of
  contrib's automatic lint discovery; this is required when calling
  `micronaut_test` from a symbolic macro.

See [CONTRIBUTING.md](CONTRIBUTING.md) to develop or release the rules.
