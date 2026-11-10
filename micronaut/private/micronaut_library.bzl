"""Micronaut Java library macro."""

load("@rules_java//java:java_library.bzl", "java_library")
load(
    ":common.bzl",
    "MICRONAUT_JAVACOPTS",
    _CORE = "CORE",
    _CORE_RUNTIME = "CORE_RUNTIME",
    _java_sources = "java_sources",
)

def micronaut_library(name, srcs = [], deps = [], runtime_deps = [], javacopts = [], **kwargs):
    """A Java library processed by Micronaut.

    Source-free resource and export aggregators are supported. Additional
    processors or visitors can be supplied through plugins.

    Args:
      name: Target name.
      srcs: Non-configurable Java sources or source jars.
      deps: Compile dependencies and features, e.g. @micronaut//:serialization.
      runtime_deps: Additional runtime dependencies, including logging or YAML support.
      javacopts: Compiler options; -parameters is added.
      **kwargs: Additional java_library attributes.
    """
    srcs = _java_sources("micronaut_library", name, srcs)
    java_library(
        name = name,
        srcs = srcs,
        # Source-free resource/export aggregators must not acquire compile deps.
        deps = deps + [_CORE] if srcs else deps,
        runtime_deps = runtime_deps + [_CORE_RUNTIME],
        javacopts = javacopts + MICRONAUT_JAVACOPTS,
        **kwargs
    )
