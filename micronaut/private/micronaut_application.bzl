"""Micronaut application macro."""

load("@rules_java//java:java_binary.bzl", "java_binary")
load(
    ":common.bzl",
    "MICRONAUT_JAVACOPTS",
    _CORE = "CORE",
    _CORE_RUNTIME = "CORE_RUNTIME",
    _java_sources = "java_sources",
    _runtime_label = "runtime_label",
)

def micronaut_application(
        name,
        main_class,
        srcs = [],
        deps = [],
        runtime_deps = [],
        javacopts = [],
        runtime = "netty",
        **kwargs):
    """A runnable Micronaut application (bazel run, <name>_deploy.jar).

    Inline sources are compiled as micronaut_library compiles them.
    With no srcs, put compiled libraries in runtime_deps.

    Args:
      name: Target name.
      main_class: Class that calls Micronaut.run(...).
      srcs: Non-configurable Java sources or source jars.
      deps: Compile dependencies and Micronaut features for inline sources.
      runtime_deps: Additional runtime dependencies, including logging or YAML support.
      javacopts: Compiler options; -parameters is added.
      runtime: Server runtime: netty, jetty, tomcat, undertow, or none.
      **kwargs: Additional java_binary attributes.
    """
    srcs = _java_sources("micronaut_application", name, srcs)
    runtime_dep = _runtime_label(runtime)
    java_binary(
        name = name,
        main_class = main_class,
        srcs = srcs,
        deps = deps + [_CORE] if srcs else deps,
        runtime_deps = runtime_deps + [runtime_dep, _CORE_RUNTIME],
        javacopts = javacopts + MICRONAUT_JAVACOPTS,
        **kwargs
    )
