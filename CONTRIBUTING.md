# Contributing

## Setup

Install [pre-commit](https://pre-commit.com/#installation) and enable the hooks:

```sh
pre-commit install
pre-commit run --all-files
```

The hooks format Starlark with buildifier, Markdown with Prettier and YAML with
yamlfmt. CI runs the same checks.

Pull request titles follow [Conventional Commits](https://www.conventionalcommits.org);
release versions are derived from them.

## Testing

```sh
bazel test //...                     # Rule unit tests and API documentation
(cd e2e/smoke && bazel test //...)   # Micronaut 5 on JDK 25
(cd examples/hello-world && bazel test //... && bazel build //:app_deploy.jar)
```

The example's tests use an in-memory H2 database, so Docker is not required.
The smoke suite's MongoDB, Azure Cosmos DB and Hibernate Reactive tests use Testcontainers and
require a running Docker-compatible container runtime.

CI also repeats the smoke suite with `rules_java` forced to the minimum declared
in the root `MODULE.bazel`. This prevents transitive dependencies from silently
upgrading the version under test.

`e2e/smoke` is a separate module that uses this checkout through
`local_path_override`. It holds one package per feature under `features/`, named
after the feature with hyphens for underscores, and rule behaviour tests under
`macros/`. Each feature package has a `:tests` suite:

```sh
(cd e2e/smoke && bazel test //features/data-jdbc:tests)
```

When adding tests:

- Compile the library and its consumer as separate targets, so generated types
  and metadata are exercised across Bazel targets.
- Prefer a test that runs. Use `build_test` only when a feature's infrastructure
  cannot be provided by a test fixture; it checks processor wiring, not runtime
  behaviour.
- A `build_test` that names generated implementation classes, such as
  `BookRepository$Intercepted`, needs `export_full_jar = True` on the library.
  Use `micronaut_full_jar_deps` when only particular consumers should opt in.
  Do not set `generates_api` on a feature to make such a test compile.
- Keep tests self-contained, with in-memory backends, temporary storage, or
  Testcontainers for databases that cannot run in memory.

After changing Maven artifacts or the platform BOM, repin and commit both
lockfiles:

```sh
(cd e2e/smoke && REPIN=1 bazel run @maven//:pin)
```

Keep `bzl_library` targets in sync with `load()` statements; the
[API documentation](docs/BUILD.bazel) is extracted through them.

## Changing features

Features are defined in [features.bzl](micronaut/private/features.bzl). A
feature contains its annotation processors, the APIs that consumers compile
against, and libraries required by generated code. It does not choose
interchangeable implementations such as connection pools, database drivers,
cache or logging backends: a dependency embedded in a feature reaches every
consumer and is hard to remove. Implementation-specific features, such as
`serialization_jackson`, are separate and opt-in.

Set `generates_api` only when users write the generated type's name in their own
source, as with SourceGen builders, the JPA metamodel and Jakarta EL expression
registries. It runs the feature's processors a second time, in header
compilation, for every library that selects the feature.

Every feature needs a package in `e2e/smoke/features`, its artifacts in
`e2e/smoke/MODULE.bazel`, and a row in the README table.

[FeatureCatalogue.java](tools/feature_catalogue/FeatureCatalogue.java) proposes a feature
definition from Micronaut Starter's metadata. Bazel supplies Java 25 and the
tool's dependencies:

```sh
bazel run //tools/feature_catalogue                   # List Starter's features as JSON
bazel run //tools/feature_catalogue -- data-r2dbc     # Propose a _feature(...) entry
```

The catalogue has a separate Maven dependency set, used only when developing
this repository. After changing its artifacts in `MODULE.bazel`, repin it and
commit `tools/feature_catalogue/maven_install.json` and `MODULE.bazel.lock`:

```sh
REPIN=1 bazel run @feature_catalogue_maven//:pin
```

Treat the output as a starting point and review it against the policy above.
Keep the tool's `CORE_API` and `CORE_PROCESSORS` in sync with the `core`
feature.

## Using a local checkout

In the consuming project's `MODULE.bazel`:

```starlark
local_path_override(
    module_name = "rules_micronaut",
    path = "/path/to/rules_micronaut",
)
```
