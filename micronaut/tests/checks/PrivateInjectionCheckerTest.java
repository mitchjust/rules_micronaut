package rules_micronaut.checks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compiles small sources with {@link PrivateInjectionChecker} and checks its diagnostics. Plain
 * main() so the ruleset needs no JUnit dependency of its own.
 */
public class PrivateInjectionCheckerTest {
  // Stand-ins for the real annotations; the checker matches by name.
  private static final String INJECT = "package jakarta.inject; public @interface Inject {}";
  private static final String POST_CONSTRUCT =
      "package jakarta.annotation; public @interface PostConstruct {}";
  private static final String PRE_DESTROY =
      "package jakarta.annotation; public @interface PreDestroy {}";

  private static final List<String> failures = new ArrayList<>();

  public static void main(String[] args) {
    expectErrors(
        "private @Inject method in open class",
        "class A { @jakarta.inject.Inject private void setDep(Object dep) {} }",
        1);
    expectErrors(
        "private @PostConstruct method",
        "class A { @jakarta.annotation.PostConstruct private void init() {} }",
        1);
    expectErrors(
        "private @PreDestroy method in abstract class",
        "abstract class A { @jakarta.annotation.PreDestroy private void close() {} }",
        1);
    expectErrors(
        "every private method is reported",
        "class A { @jakarta.annotation.PostConstruct private void a() {}"
            + " @jakarta.annotation.PreDestroy private void b() {} }",
        2);
    expectErrors(
        "private field is kept in header jars",
        "class A { @jakarta.inject.Inject private Object dep; }",
        0);
    expectErrors(
        "package-private method is fine",
        "class A { @jakarta.annotation.PostConstruct void init() {} }",
        0);
    expectErrors(
        "protected method is fine",
        "class A { @jakarta.inject.Inject protected void setDep(Object dep) {} }",
        0);
    expectErrors(
        "final class cannot be subclassed",
        "final class A { @jakarta.annotation.PostConstruct private void init() {} }",
        0);
    expectErrors(
        "private constructor is not inherited",
        "class A { @jakarta.inject.Inject private A() {} }",
        0);
    expectErrors(
        "suppressed on member",
        "class A { @SuppressWarnings(\"MicronautPrivateInjection\")"
            + " @jakarta.annotation.PostConstruct private void init() {} }",
        0);
    expectErrors(
        "suppressed on class",
        "@SuppressWarnings(\"MicronautPrivateInjection\")"
            + " class A { @jakarta.annotation.PostConstruct private void init() {} }",
        0);
    expectDiagnostics(
        "warn reports a warning, not an error",
        "warn",
        "class A { @jakarta.annotation.PostConstruct private void init() {} }",
        0,
        1);
    expectDiagnostics(
        "off reports nothing",
        "off",
        "class A { @jakarta.annotation.PostConstruct private void init() {} }",
        0,
        0);

    if (!failures.isEmpty()) {
      failures.forEach(System.err::println);
      System.exit(1);
    }
    System.out.println("All PrivateInjectionChecker cases passed.");
  }

  private static void expectErrors(String name, String source, int errors) {
    expectDiagnostics(name, null, source, errors, 0);
  }

  private static void expectDiagnostics(
      String name, String mode, String source, int errors, int warnings) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    List<String> options = new ArrayList<>(List.of("-proc:only"));
    if (mode != null) {
      options.add("-A" + PrivateInjectionChecker.OPTION + "=" + mode);
    }
    List<JavaFileObject> sources =
        List.of(
            source("A", "package test; " + source),
            source("Inject", INJECT),
            source("PostConstruct", POST_CONSTRUCT),
            source("PreDestroy", PRE_DESTROY));
    JavaCompiler.CompilationTask task =
        compiler.getTask(null, null, diagnostics, options, null, sources);
    task.setProcessors(List.of(new PrivateInjectionChecker()));
    task.call();

    List<Diagnostic<? extends JavaFileObject>> ours =
        diagnostics.getDiagnostics().stream()
            .filter(d -> d.getMessage(null).contains(PrivateInjectionChecker.SUPPRESSION))
            .collect(Collectors.toList());
    long actualErrors = ours.stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).count();
    long actualWarnings =
        ours.stream().filter(d -> d.getKind() == Diagnostic.Kind.WARNING).count();
    if (actualErrors != errors || actualWarnings != warnings) {
      failures.add(
          String.format(
              "FAIL %s: expected %d errors/%d warnings, got %d/%d: %s",
              name, errors, warnings, actualErrors, actualWarnings, ours));
    }
  }

  private static JavaFileObject source(String name, String code) {
    return new SimpleJavaFileObject(
        URI.create("string:///" + name + ".java"), JavaFileObject.Kind.SOURCE) {
      @Override
      public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
      }
    };
  }
}
