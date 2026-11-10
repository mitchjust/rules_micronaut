import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.application.generator.ProjectGenerator;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.Scope;
import io.micronaut.starter.feature.AvailableFeatures;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.Options;
import io.micronaut.starter.options.TestFramework;
import io.micronaut.starter.util.NameUtils;
import io.micronaut.starter.util.VersionInfo;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession.CloseableSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.supplier.SessionBuilderSupplier;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;

/** Lists Starter features as JSON, or prints a filtered dependency proposal as Starlark. */
public class FeatureCatalogue {
    // Every generated feature target already exports :core. Keep this in sync with features.bzl.
    static final Set<String> CORE_API = Set.of(
            "io.micronaut:micronaut-inject",
            "io.micronaut:micronaut-core",
            "io.micronaut:micronaut-context",
            "io.micronaut:micronaut-aop",
            "jakarta.inject:jakarta.inject-api",
            "jakarta.annotation:jakarta.annotation-api",
            "io.micronaut.sourcegen:micronaut-sourcegen-annotations");
    static final Set<String> CORE_PROCESSORS = Set.of("io.micronaut:micronaut-inject-java");

    // Export policy, not Maven exclusions: these jars remain in the dependency graph.
    // Use exact coordinates: Persistence, Validation, Transaction, etc. are feature APIs.
    static final Set<String> EXCLUDED_API = Set.of(
            "org.slf4j:slf4j-api",
            "org.jspecify:jspecify");

    // Applications choose pools and management endpoints. A named request remains an opt-in.
    static final Set<String> OPTIONAL_COMPANIONS = Set.of(
            "jdbc-hikari", "jdbc-tomcat", "jdbc-dbcp", "management");

    // If a pool is explicitly requested, emit its integration as runtime support.
    static final Set<String> RUNTIME_ONLY = Set.of(
            "io.micronaut.sql:micronaut-jdbc-hikari",
            "io.micronaut.sql:micronaut-jdbc-tomcat",
            "io.micronaut.sql:micronaut-jdbc-dbcp");

    // Local processor/generated-code requirements not declared by these Starter features.
    static final Map<String, List<Dependency>> REQUIRED_DEPENDENCIES = Map.of(
            "data-r2dbc", List.of(Dependency.builder().groupId("com.fasterxml.jackson.core")
                    .artifactId("jackson-annotations").runtime().build()),
            "multi-tenancy", List.of(
                    Dependency.builder().groupId("io.micronaut.multitenancy")
                            .artifactId("micronaut-multitenancy-annotations").annotationProcessor().build(),
                    Dependency.builder().groupId("org.slf4j").artifactId("slf4j-api").compile().build()));
    static final Map<String, Set<String>> API_EXCEPTIONS = Map.of(
            "multi-tenancy", Set.of("org.slf4j:slf4j-api"));

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("Usage: bazel run //tools/feature_catalogue -- [feature-name]");
            System.out.println("No arguments: list default application features as JSON.");
            System.out.println("Feature name: print a filtered _feature(...) proposal including companion dependencies.");
            return;
        }
        if (args.length > 1) {
            System.err.println("Expected at most one Starter feature name, for example data-r2dbc.");
            System.exit(1);
            return;
        }

        try (ApplicationContext context = ApplicationContext.run()) {
            if (args.length == 0) {
                printCatalogue(context);
            } else {
                printFeature(context, args[0]);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printCatalogue(ApplicationContext context) throws Exception {
        AvailableFeatures available = context.getBean(AvailableFeatures.class, Qualifiers.byName("default"));
        var features = available.getFeatures()
                .sorted(Comparator.comparing(Feature::getName))
                .map(FeatureCatalogue::describe)
                .toList();
        Map<String, Object> catalogue = new LinkedHashMap<>();
        catalogue.put("micronautVersion", VersionInfo.getMicronautVersion());
        catalogue.put("applicationType", "default");
        catalogue.put("features", features);
        System.out.println(context.getBean(JsonMapper.class).writeValueAsString(catalogue));
    }

    private static void printFeature(ApplicationContext context, String name) throws Exception {
        FeatureDependencies selected = featureDependencies(context, name);
        if (!selected.companions().isEmpty()) {
            System.err.println("Included companion features: " + String.join(", ", selected.companions()));
        }
        if (!selected.omittedCompanions().isEmpty()) {
            System.err.println("Omitted application choices: " + String.join(", ", selected.omittedCompanions()));
        }
        FeatureEntry entry;
        try (MavenDependencies resolver = new MavenDependencies()) {
            CompileDependencies resolved = resolver.resolve(selected.dependencies(), selected.properties(),
                    VersionInfo.getMicronautVersion(), runtimeRoots(selected.dependencies(), name));
            entry = curate(resolved, selected.dependencies(), name);
        }
        System.out.printf("    \"%s\": _feature(%n", name.replace('-', '_'));
        printCoordinates("api", entry.api());
        printCoordinates("processors", entry.processors());
        printCoordinates("runtime", entry.runtime());
        if (!entry.compileOnly().isEmpty()) {
            printCoordinates("compile_only", entry.compileOnly());
        }
        System.out.println("    ),");
    }

    static FeatureDependencies featureDependencies(ApplicationContext context, String name) {
        Feature feature = context.getBeansOfType(Feature.class).stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Starter feature: " + name));
        ApplicationType type = Arrays.stream(ApplicationType.values())
                .filter(feature::supports)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No supported application type for feature: " + name));
        Options options = new Options(Language.JAVA, TestFramework.JUNIT, BuildTool.MAVEN);
        GeneratorContext generator = context.getBean(ProjectGenerator.class).createGeneratorContext(
                type,
                NameUtils.parse("example.catalogue"),
                options,
                null,
                List.of(name),
                ConsoleOutput.NOOP);

        // Discover just this feature's recursive companions, without application defaults
        // (server, logging, test framework, etc.). Use the full context for apply-time lookups.
        FeatureContext selection = new FeatureContext(options, type, null, Set.of(feature));
        selection.processSelectedFeatures();
        Set<Feature> companions = selection.getFinalFeatures(ConsoleOutput.NOOP);
        SortedSet<String> included = new TreeSet<>();
        SortedSet<String> omittedCompanions = new TreeSet<>();
        generator.getFeatures().getFeatures().stream()
                .filter(companions::contains)
                .sorted(Comparator.comparingInt(Feature::getOrder).thenComparing(Feature::getName))
                .forEach(candidate -> {
                    // Selecting a driver, pool, or management feature explicitly still includes it.
                    if (candidate != feature && (candidate instanceof DatabaseDriverFeature
                            || OPTIONAL_COMPANIONS.contains(candidate.getName()))) {
                        omittedCompanions.add(candidate.getName());
                        return;
                    }
                    candidate.apply(generator);
                    if (candidate != feature) {
                        included.add(candidate.getName());
                    }
                });
        REQUIRED_DEPENDENCIES.getOrDefault(name, List.of()).forEach(generator::addDependency);
        Map<String, String> properties = new LinkedHashMap<>();
        generator.getBuildProperties().getProperties().stream()
                .filter(property -> !property.isComment())
                .forEach(property -> properties.put(property.getKey(), property.getValue()));
        return new FeatureDependencies(List.copyOf(generator.getDependencies()), properties, included, omittedCompanions);
    }

    static SortedSet<String> coordinates(Collection<Dependency> dependencies, Scope... scopes) {
        return new TreeSet<>(dependencies.stream()
                .filter(dependency -> !dependency.isPom() && Arrays.asList(scopes).contains(dependency.getScope()))
                .map(FeatureCatalogue::coordinate)
                .toList());
    }

    private static String coordinate(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId();
    }

    static Set<String> runtimeRoots(Collection<Dependency> dependencies) {
        return runtimeRoots(dependencies, "");
    }

    static Set<String> runtimeRoots(Collection<Dependency> dependencies, String feature) {
        SortedSet<String> roots = coordinates(dependencies, Scope.API, Scope.COMPILE);
        roots.removeIf(coordinate -> !RUNTIME_ONLY.contains(coordinate) && !EXCLUDED_API.contains(coordinate));
        roots.removeAll(API_EXCEPTIONS.getOrDefault(feature, Set.of()));
        return roots;
    }

    static FeatureEntry curate(CompileDependencies resolved, Collection<Dependency> dependencies) {
        return curate(resolved, dependencies, "");
    }

    static FeatureEntry curate(CompileDependencies resolved, Collection<Dependency> dependencies, String feature) {
        SortedSet<String> api = new TreeSet<>(resolved.api());
        SortedSet<String> compileOnly = new TreeSet<>(resolved.compileOnly());
        for (Set<String> exclusions : List.of(CORE_API, EXCLUDED_API, RUNTIME_ONLY)) {
            Set<String> filtered = new TreeSet<>(exclusions);
            filtered.removeAll(API_EXCEPTIONS.getOrDefault(feature, Set.of()));
            api.removeAll(filtered);
            compileOnly.removeAll(filtered);
        }
        SortedSet<String> processors = coordinates(dependencies, Scope.ANNOTATION_PROCESSOR);
        processors.removeAll(CORE_PROCESSORS);
        SortedSet<String> runtime = coordinates(dependencies, Scope.RUNTIME);
        // Preserve direct roots whose API export was suppressed, along with their runtime closure.
        runtime.addAll(runtimeRoots(dependencies, feature));
        runtime.removeAll(CORE_API);
        runtime.removeAll(api);
        return new FeatureEntry(api, processors, runtime, compileOnly);
    }

    private static void printCoordinates(String field, Collection<String> coordinates) {
        if (coordinates.isEmpty()) {
            System.out.printf("        %s = [],%n", field);
        } else {
            System.out.printf("        %s = [%n", field);
            coordinates.forEach(coordinate -> System.out.printf("            \"%s\",%n", coordinate));
            System.out.println("        ],");
        }
    }

    record CompileDependencies(SortedSet<String> api, SortedSet<String> compileOnly) {}
    record FeatureDependencies(List<Dependency> dependencies, Map<String, String> properties,
                               SortedSet<String> companions, SortedSet<String> omittedCompanions) {}
    record FeatureEntry(SortedSet<String> api, SortedSet<String> processors, SortedSet<String> runtime,
                        SortedSet<String> compileOnly) {}

    /** Maven's model builder handles parent POMs, imported BOMs, and version properties. */
    static class MavenDependencies implements AutoCloseable {
        private final RepositorySystem system = new RepositorySystemSupplier().get();
        private final CloseableSession session;
        private final List<RemoteRepository> repositories;

        MavenDependencies() {
            this(Path.of(System.getProperty("user.home"), ".m2", "repository"), List.of(
                    new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build()));
        }

        MavenDependencies(Path localRepository, List<RemoteRepository> repositories) {
            this.repositories = repositories;
            session = new SessionBuilderSupplier(system).get()
                    .withLocalRepositoryBaseDirectories(localRepository)
                    // Missing or invalid POMs must not produce an incomplete catalogue silently.
                    .setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false))
                    .build();
        }

        CompileDependencies resolve(Collection<Dependency> declarations, Map<String, String> properties,
                                    String platformVersion) throws Exception {
            return resolve(declarations, properties, platformVersion, Set.of());
        }

        CompileDependencies resolve(Collection<Dependency> declarations, Map<String, String> properties,
                                    String platformVersion, Set<String> runtimeRoots) throws Exception {
            var main = declarations.stream()
                    .filter(dependency -> mavenScope(dependency.getScope()) != null)
                    .sorted(Dependency.COMPARATOR)
                    .toList();
            if (main.stream().noneMatch(dependency -> !dependency.isPom())) {
                return new CompileDependencies(new TreeSet<>(), new TreeSet<>());
            }

            Map<String, org.eclipse.aether.graph.Dependency> managed = new LinkedHashMap<>();
            addManagement(managed, new DefaultArtifact(
                    "io.micronaut.platform", "micronaut-platform", "pom", platformVersion));

            // Feature BOMs act as project-level imports, overriding the platform's management.
            // Maven gives the first import precedence when multiple imports manage the same artifact.
            Map<String, org.eclipse.aether.graph.Dependency> featureManaged = new LinkedHashMap<>();
            for (Dependency dependency : main) {
                if (dependency.isPom()) {
                    addManagement(featureManaged, artifact(dependency, managed, properties));
                }
            }
            managed.putAll(featureManaged);

            List<org.eclipse.aether.graph.Dependency> roots = new ArrayList<>();
            for (Dependency dependency : main) {
                if (dependency.isPom()) {
                    continue;
                }
                Artifact artifact = artifact(dependency, managed, properties);
                var management = managed.get(key(artifact));
                List<Exclusion> exclusions = new ArrayList<>();
                if (management != null) {
                    exclusions.addAll(management.getExclusions());
                }
                if (dependency.getExclusions() != null) {
                    for (Dependency exclusion : dependency.getExclusions()) {
                        exclusions.add(new Exclusion(
                                exclusion.getGroupId() == null ? "*" : exclusion.getGroupId(),
                                exclusion.getArtifactId(), "*", "*"));
                    }
                }
                roots.add(new org.eclipse.aether.graph.Dependency(
                        artifact, runtimeRoots.contains(coordinate(dependency)) ? "runtime"
                                : mavenScope(dependency.getScope()), false, exclusions));
            }

            // Collect metadata only: no application or processor jars are downloaded here.
            DependencyNode graph = system.collectDependencies(session, new CollectRequest(
                    roots, new ArrayList<>(managed.values()), repositories)).getRoot();
            CompileDependencies result = new CompileDependencies(new TreeSet<>(), new TreeSet<>());
            collect(graph, result, Collections.newSetFromMap(new IdentityHashMap<>()));
            return result;
        }

        private void addManagement(Map<String, org.eclipse.aether.graph.Dependency> managed, Artifact bom)
                throws Exception {
            var descriptor = system.readArtifactDescriptor(session,
                    new ArtifactDescriptorRequest(bom, repositories, null));
            for (var dependency : descriptor.getManagedDependencies()) {
                managed.putIfAbsent(key(dependency.getArtifact()), dependency);
            }
        }

        private static Artifact artifact(Dependency dependency,
                                         Map<String, org.eclipse.aether.graph.Dependency> managed,
                                         Map<String, String> properties) {
            Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.isPom() ? "pom" : "jar", "");
            String version = dependency.getVersion();
            if (version == null && dependency.getVersionProperty() != null) {
                version = properties.get(dependency.getVersionProperty());
            }
            if (version == null) {
                var management = managed.get(key(artifact));
                if (management != null) {
                    version = management.getArtifact().getVersion();
                }
            }
            Set<String> visited = new TreeSet<>();
            while (version != null && version.startsWith("${") && version.endsWith("}") && visited.add(version)) {
                version = properties.get(version.substring(2, version.length() - 1));
            }
            if (version == null || version.isBlank() || version.contains("${")) {
                throw new IllegalArgumentException("No resolved version for " + dependency.getGroupId() + ":"
                        + dependency.getArtifactId() + " in Starter's declarations or the selected BOMs.");
            }
            return artifact.setVersion(version);
        }

        private static String key(Artifact artifact) {
            return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                    + artifact.getExtension() + ":" + artifact.getClassifier();
        }

        private static String mavenScope(Scope scope) {
            if (Scope.API.equals(scope) || Scope.COMPILE.equals(scope)) {
                return "compile";
            }
            if (Scope.COMPILE_ONLY.equals(scope)) {
                return "provided";
            }
            return Scope.RUNTIME.equals(scope) ? "runtime" : null;
        }

        private static void collect(DependencyNode node, CompileDependencies result, Set<DependencyNode> visited) {
            if (!visited.add(node) || node.getData().containsKey(ConflictResolver.NODE_DATA_WINNER)) {
                return;
            }
            var dependency = node.getDependency();
            if (dependency != null) {
                SortedSet<String> coordinates = switch (dependency.getScope()) {
                    case "compile" -> result.api();
                    case "provided" -> result.compileOnly();
                    default -> null;
                };
                Artifact artifact = dependency.getArtifact();
                if (coordinates != null && !artifact.getExtension().equals("pom")) {
                    if (!artifact.getExtension().equals("jar") || !artifact.getClassifier().isEmpty()) {
                        throw new IllegalArgumentException("Cannot represent " + artifact
                                + " as an unclassified group:artifact jar in features.bzl.");
                    }
                    coordinates.add(artifact.getGroupId() + ":" + artifact.getArtifactId());
                }
            }
            node.getChildren().forEach(child -> collect(child, result, visited));
        }

        @Override
        public void close() {
            try {
                session.close();
            } finally {
                system.shutdown();
            }
        }
    }

    private static Map<String, Object> describe(Feature feature) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", feature.getName());
        result.put("title", feature.getTitle());
        result.put("category", feature.getCategory());
        result.put("description", feature.getDescription());
        return result;
    }
}
