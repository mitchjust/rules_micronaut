package rules_micronaut.checks;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Rejects private injection and lifecycle methods in classes that can be subclassed.
 *
 * <p>Bazel compiles each target against the header jars of its dependencies, and header jars omit
 * private methods. When a bean's superclass is in another target, Micronaut cannot see the
 * superclass's private {@code @Inject}, {@code @PostConstruct} or {@code @PreDestroy} methods, so
 * it silently never calls them. Gradle and Maven compile against full class files, so the same
 * code works there. Private fields are unaffected: MICRONAUT_JAVACOPTS keeps them in header jars.
 *
 * <p>The superclass's own compilation is the only place the method is visible, so that is where
 * this processor reports it. It never claims annotations, so Micronaut's processors still see them.
 */
public class PrivateInjectionChecker extends AbstractProcessor {
  static final String SUPPRESSION = "MicronautPrivateInjection";

  /** {@code -Arules_micronaut.private_injection=error|warn|off}; the default is error. */
  static final String OPTION = "rules_micronaut.private_injection";

  private static final Set<String> ANNOTATIONS =
      Set.of(
          "jakarta.inject.Inject",
          "javax.inject.Inject",
          "jakarta.annotation.PostConstruct",
          "jakarta.annotation.PreDestroy",
          "javax.annotation.PostConstruct",
          "javax.annotation.PreDestroy");

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ANNOTATIONS;
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of(OPTION);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    String mode = processingEnv.getOptions().getOrDefault(OPTION, "error");
    if (mode.equals("off")) {
      return false;
    }
    Diagnostic.Kind kind = mode.equals("warn") ? Diagnostic.Kind.WARNING : Diagnostic.Kind.ERROR;
    for (TypeElement annotation : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
        if (element.getKind() == ElementKind.METHOD
            && element.getModifiers().contains(Modifier.PRIVATE)
            && canBeSubclassed(element.getEnclosingElement())
            && !isSuppressed(element)) {
          report(kind, element, annotation);
        }
      }
    }
    return false;
  }

  private void report(Diagnostic.Kind kind, Element element, TypeElement annotation) {
    processingEnv
        .getMessager()
        .printMessage(
            kind,
            String.format(
                "[%s] private @%s method '%s' in non-final class %s. Under Bazel, subclasses in"
                    + " other targets compile against a header jar without private methods, so"
                    + " Micronaut never calls it for them. Make it package-private or protected,"
                    + " or make the class final. To keep it private, add"
                    + " @SuppressWarnings(\"%s\") and wrap this target in"
                    + " micronaut_full_jar_deps wherever the class is subclassed.",
                SUPPRESSION,
                annotation.getSimpleName(),
                element.getSimpleName(),
                element.getEnclosingElement(),
                SUPPRESSION),
            element);
  }

  private static boolean canBeSubclassed(Element type) {
    return type.getKind() == ElementKind.CLASS && !type.getModifiers().contains(Modifier.FINAL);
  }

  private static boolean isSuppressed(Element element) {
    for (Element e = element; e != null; e = e.getEnclosingElement()) {
      SuppressWarnings suppress = e.getAnnotation(SuppressWarnings.class);
      if (suppress != null && Set.of(suppress.value()).contains(SUPPRESSION)) {
        return true;
      }
    }
    return false;
  }
}
