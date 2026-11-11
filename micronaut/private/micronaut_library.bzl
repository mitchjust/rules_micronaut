"""Micronaut Java library macro."""

load("@rules_java//java:java_library.bzl", "java_library")
load(
    ":common.bzl",
    "MICRONAUT_JAVACOPTS",
    _CORE = "CORE",
    _CORE_RUNTIME = "CORE_RUNTIME",
    _java_sources = "java_sources",
)
load(":full_jar_deps.bzl", "full_jar_library")

def micronaut_library(name, srcs = [], deps = [], runtime_deps = [], javacopts = [], export_full_jar = False, **kwargs):
    """A Java library processed by Micronaut.

    Source-free resource and export aggregators are supported. Additional
    processors or visitors can be supplied through plugins. Set export_full_jar to
    expose generated members that are absent from header jars, such as Lombok's.

    Args:
      name: Target name.
      srcs: Non-configurable Java sources or source jars.
      deps: Compile dependencies and features, e.g. @micronaut//:serialization.
      runtime_deps: Additional runtime dependencies, including logging or YAML support.
      javacopts: Compiler options; -parameters is added.
      export_full_jar: Non-configurable boolean. Compile dependants against this library's
        full jar (including its exports), preserving generated members and private
        methods. Defaults to False. Implementation changes can recompile dependants.
      **kwargs: Additional java_library attributes.
    """
    if type(export_full_jar) != "bool":
        fail("micronaut_library %r: export_full_jar must be a non-configurable boolean" % name)
    srcs = _java_sources("micronaut_library", name, srcs)
    compile_name = name
    if export_full_jar:
        compile_name = name + "__compiled"

        # Keep visibility, testonly and compatibility constraints on the public
        # target as well as the underlying compilation. Java-specific attributes
        # remain on java_library, and its providers carry their results through.
        public_kwargs = {
            key: kwargs[key]
            for key in [
                "visibility",
                "testonly",
                "tags",
                "deprecation",
                "compatible_with",
                "restricted_to",
                "target_compatible_with",
            ]
            if key in kwargs
        }
        full_jar_library(name = name, actual = ":" + compile_name, **public_kwargs)

        # Preserve the conventional explicit jar labels as well as DefaultInfo.
        for suffix in [".jar", "-src.jar"]:
            native.alias(
                name = "lib" + name + suffix,
                actual = ":lib" + compile_name + suffix,
                **public_kwargs
            )
        kwargs["visibility"] = ["//visibility:private"]
    java_library(
        name = compile_name,
        srcs = srcs,
        # Source-free resource/export aggregators must not acquire compile deps.
        deps = deps + [_CORE] if srcs else deps,
        runtime_deps = runtime_deps + [_CORE_RUNTIME],
        javacopts = javacopts + MICRONAUT_JAVACOPTS,
        **kwargs
    )
