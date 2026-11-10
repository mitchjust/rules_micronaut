"""Micronaut JUnit test macro."""

load("@contrib_rules_jvm//java:defs.bzl", "java_junit5_test")
load(
    ":common.bzl",
    "MICRONAUT_JAVACOPTS",
    _CORE = "CORE",
    _CORE_RUNTIME = "CORE_RUNTIME",
    _java_sources = "java_sources",
    _runtime_label = "runtime_label",
)

_TEST_JUNIT5 = Label("@micronaut//:_macro_test_junit5")

def _derive_test_class(name, srcs):
    candidates = []
    for src in srcs:
        if src.name.split("/")[-1] != name + ".java":
            continue
        path = "/" + src.package + "/" + src.name[:-len(".java")]
        for root in ["src/test/java/", "src/main/java/", "java/", "javatests/"]:
            if "/" + root in path:
                candidates.append(path.split("/" + root, 1)[1].replace("/", "."))
                break
    if len(candidates) == 1:
        return candidates[0]
    fail("micronaut_test %r: set `test_class`; it can only be derived from a single matching Java source under src/test/java/, src/main/java/, java/ or javatests/" % name)

def micronaut_test(
        name,
        srcs = [],
        deps = [],
        runtime_deps = [],
        javacopts = [],
        runtime = "netty",
        test_class = None,
        **kwargs):
    """A Java JUnit Platform test whose sources are processed by Micronaut.

    Uses contrib_rules_jvm's runner and the JUnit runtime selected by your BOM.
    Supports IntelliJ test selection, JUnit XML reports and Bazel test sharding.

    Args:
      name: Target name.
      srcs: Non-configurable Java sources or source jars.
      deps: Compile dependencies and Micronaut features.
      runtime_deps: Additional runtime dependencies, including logging or YAML support.
      javacopts: Compiler options; -parameters is added.
      runtime: Server runtime: netty, jetty, tomcat, undertow, or none.
      test_class: Non-configurable class name; otherwise inferred from a source matching name.
      **kwargs: Additional java_junit5_test attributes, including JUnit tag and engine filters.
    """
    srcs = _java_sources("micronaut_test", name, srcs)
    runtime_dep = _runtime_label(runtime)
    if test_class != None and type(test_class) != "string":
        fail("`test_class` must be a non-configurable string")
    testonly = kwargs.get("testonly")
    kwargs["testonly"] = True if testonly == None else testonly
    java_junit5_test(
        name = name,
        srcs = srcs,
        test_class = test_class or _derive_test_class(name, srcs),
        deps = deps + [_TEST_JUNIT5, _CORE] if srcs else deps,
        runtime_deps = runtime_deps + [_TEST_JUNIT5, _CORE_RUNTIME, runtime_dep],
        javacopts = javacopts + MICRONAUT_JAVACOPTS,
        **kwargs
    )
