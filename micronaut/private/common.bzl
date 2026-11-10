"""Shared helpers for Micronaut's Java macros.

Every macro that compiles sources adds Micronaut core, runs Micronaut's
processors, and compiles with MICRONAUT_JAVACOPTS.

Java attributes are forwarded to the underlying macros. Configurable values
must only be forwarded or concatenated.
"""

# Reserved aliases avoid duplicates when callers also supply the public labels,
# including through select().
CORE = Label("@micronaut//:_macro_core")
CORE_RUNTIME = Label("@micronaut//:_macro_core_runtime")

MICRONAUT_JAVACOPTS = [
    # Micronaut relies on parameter names when binding controller arguments.
    "-parameters",
    # Keep private fields in this target's header jar. Micronaut reads the
    # superclasses of the classes it processes, and needs their private
    # injection points and the annotations on their private fields.
    "-XDturbine.emitPrivateFields",
]

_UNSUPPORTED_SOURCES = (".kt", ".kts", ".groovy", ".scala")

def java_sources(kind, name, srcs):
    """Resolves source labels in the caller's package and rejects non-Java files.

    Args:
      kind: Name of the macro, used in the error message.
      name: Name of the target being declared.
      srcs: Non-configurable source labels supplied to the macro.

    Returns:
      The resolved Java source labels.
    """
    if type(srcs) not in ("list", "tuple"):
        fail("%s %r: `srcs` must be a non-configurable list" % (kind, name))
    srcs = [native.package_relative_label(src) for src in srcs]
    for src in srcs:
        if src.name.endswith(_UNSUPPORTED_SOURCES):
            fail(("%s %r: %r is not a Java source. rules_micronaut supports Java only; " +
                  "Kotlin, Groovy (including Spock) and Scala are not supported.") % (kind, name, src))
    return srcs

def runtime_label(runtime):
    """Resolves a supported server runtime.

    Args:
      runtime: A non-configurable server runtime name.

    Returns:
      The runtime dependency label.
    """
    if runtime not in ("jetty", "netty", "none", "tomcat", "undertow"):
        fail("`runtime` must be one of: jetty, netty, none, tomcat, undertow")
    return Label("@micronaut//:_macro_runtime_" + runtime)
