"""Module extension that configures rules_micronaut for Micronaut 5+.

```starlark
maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
maven.install(
    boms = ["io.micronaut.platform:micronaut-platform:5.2.5"],
    artifacts = [...],  # see micronaut/private/features.bzl for per-feature coordinates
    resolver = "maven",
)
use_repo(maven, "maven")

micronaut = use_extension("@rules_micronaut//micronaut:extensions.bzl", "micronaut")
micronaut.maven(repo = "@maven")
use_repo(micronaut, "micronaut")
```

The Micronaut version comes from the user's own Maven resolution (normally the
platform BOM). rules_micronaut never adds artifacts to the user's Maven repo.
Select features on each target through deps, e.g. `@micronaut//:serialization`.
Select optional runtime libraries directly through runtime_deps, e.g.
`@maven//:org_yaml_snakeyaml` and `@maven//:ch_qos_logback_logback_classic`.
"""

load("//micronaut/private:micronaut_repo.bzl", "micronaut_repo")

_maven = tag_class(
    doc = "Selects the Maven repository used by Micronaut. Only honoured in the root module.",
    attrs = {
        "repo": attr.label(
            mandatory = True,
            doc = "Any label in the rules_jvm_external repo that resolves Micronaut, e.g. `@maven`. " +
                  "Only its repository is used.",
        ),
    },
)

def _micronaut_impl(mctx):
    maven = None
    for mod in mctx.modules:
        if not mod.is_root:
            if mod.tags.maven:
                # buildifier: disable=print
                print("WARNING: rules_micronaut: ignoring micronaut.maven() in module %r; " % mod.name +
                      "only the root module configures Micronaut.")
            continue
        for tag in mod.tags.maven:
            if maven:
                fail("micronaut.maven() may only be called once")
            maven = tag

    if maven:
        micronaut_repo(
            name = "micronaut",
            maven_repo = maven.repo.repo_name,
        )
    else:
        micronaut_repo(name = "micronaut")

    return mctx.extension_metadata(
        root_module_direct_deps = ["micronaut"] if mctx.root_module_has_non_dev_dependency else [],
        root_module_direct_dev_deps = [] if mctx.root_module_has_non_dev_dependency else ["micronaut"],
        reproducible = True,
    )

micronaut = module_extension(
    implementation = _micronaut_impl,
    tag_classes = {"maven": _maven},
    doc = "Generates the `@micronaut` repository of building-block targets.",
)
