"""Targets that fail analysis with an actionable message."""

def _failing_impl(ctx):
    fail(ctx.attr.message)

failing = rule(
    implementation = _failing_impl,
    attrs = {"message": attr.string(mandatory = True)},
    doc = "Fails analysis with `message`. Used as a placeholder in the generated `@micronaut` repo.",
)
