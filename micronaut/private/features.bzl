"""Maven coordinates that make up each Micronaut 5+ feature.

Versions are deliberately absent: users resolve these through their own
rules_jvm_external `maven.install`, normally with the Micronaut platform BOM
(`io.micronaut.platform:micronaut-platform`) selecting managed versions.
"""

# The annotation processors registered by micronaut-inject-java 5.2 in
# META-INF/services/javax.annotation.processing.Processor. Order matches the
# services file, which is the order Maven and plain javac run them in.
PROCESSOR_CLASSES = [
    "io.micronaut.annotation.processing.MixinVisitorProcessor",
    "io.micronaut.annotation.processing.PackageElementVisitorProcessor",
    "io.micronaut.annotation.processing.TypeElementVisitorProcessor",
    "io.micronaut.annotation.processing.AggregatingTypeElementVisitorProcessor",
    "io.micronaut.annotation.processing.BeanDefinitionInjectProcessor",
]

def _feature(api = [], processors = [], runtime = [], includes = [], compile_only = [], processor_classes = [], generates_api = False):
    return struct(
        api = api,
        processors = processors,
        runtime = runtime,
        includes = includes,
        compile_only = compile_only,
        processor_classes = processor_classes,
        generates_api = generates_api,
    )

# Each feature contributes:
#   api:        compile-time deps exported to dependants
#   processors: jars added to the consuming target's annotation processor path
#   runtime:    runtime-only deps
#   includes:   other feature targets to export, including their processors
#   compile_only: API jars exported through a neverlink library
#   processor_classes: standalone processors; empty means Micronaut visitors
#   generates_api: run these processors in header compilation so dependants can
#                  reference generated types; included features retain their own setting
# Each compilation uses one processor path, because Micronaut loads
# visitors from other modules via ServiceLoader on the processor classloader.
FEATURES = {
    "cache": _feature(
        # Cache annotations use core AOP; applications select the cache backend.
        api = ["io.micronaut.cache:micronaut-cache-core"],
    ),
    "core": _feature(
        api = [
            "io.micronaut:micronaut-inject",
            "io.micronaut:micronaut-core",
            "io.micronaut:micronaut-context",
            # @Around, MethodInterceptor and other AOP APIs.
            "io.micronaut:micronaut-aop",
            "jakarta.inject:jakarta.inject-api",
            "jakarta.annotation:jakarta.annotation-api",
            # Micronaut 5 processors generate Java sources (e.g. serde
            # serializers) annotated with @GeneratedSource, so dependants
            # compile against it directly.
            "io.micronaut.sourcegen:micronaut-sourcegen-annotations",
        ],
        processors = ["io.micronaut:micronaut-inject-java"],
        runtime = [],
    ),
    "data_azure_cosmos": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-document-model",
            "io.micronaut.data:micronaut-data-azure-cosmos",
            "io.micronaut.serde:micronaut-serde-api",
        ],
        processors = ["io.micronaut.data:micronaut-data-document-processor"],
    ),
    "data_hibernate_reactive": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-hibernate-reactive",
            "jakarta.persistence:jakarta.persistence-api",
        ],
        processors = ["io.micronaut.data:micronaut-data-processor"],
    ),
    "data_jdbc": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-jdbc",
        ],
        processors = ["io.micronaut.data:micronaut-data-processor"],
    ),
    "data_jpa": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-hibernate-jpa",
            "jakarta.persistence:jakarta.persistence-api",
        ],
        processors = ["io.micronaut.data:micronaut-data-processor"],
    ),
    "data_jpa_metamodel": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "jakarta.persistence:jakarta.persistence-api",
        ],
        generates_api = True,
        processors = ["io.micronaut.data:micronaut-data-jpa-metamodel-processor"],
    ),
    "data_mongodb": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-document-model",
            "io.micronaut.data:micronaut-data-mongodb",
            "io.micronaut.serde:micronaut-serde-api",
        ],
        processors = ["io.micronaut.data:micronaut-data-document-processor"],
    ),
    "data_r2dbc": _feature(
        api = [
            "io.micronaut.data:micronaut-data-model",
            "io.micronaut.data:micronaut-data-r2dbc",
            "io.r2dbc:r2dbc-spi",
            "io.projectreactor:reactor-core",
        ],
        processors = ["io.micronaut.data:micronaut-data-processor"],
        runtime = [
            # The R2DBC schema generator inspects Jackson property annotations.
            "com.fasterxml.jackson.core:jackson-annotations",
        ],
    ),
    "data_spring_jdbc": _feature(
        includes = ["data_jdbc"],
        api = [
            "io.micronaut.data:micronaut-data-spring-jdbc",
            "org.springframework:spring-jdbc",
            "org.springframework.data:spring-data-commons",
        ],
    ),
    "eclipsestore": _feature(
        api = ["io.micronaut.eclipsestore:micronaut-eclipsestore", "io.micronaut.eclipsestore:micronaut-eclipsestore-annotations"],
        processors = ["io.micronaut.eclipsestore:micronaut-eclipsestore-processor"],
    ),
    "guice": _feature(
        api = [
            "com.google.inject:guice",
            "io.micronaut.guice:micronaut-guice",
            "io.micronaut.guice:micronaut-guice-annotation",
        ],
        processors = ["io.micronaut.guice:micronaut-guice-processor"],
    ),
    "hibernate_jpamodelgen": _feature(
        api = ["jakarta.persistence:jakarta.persistence-api"],
        # Hibernate 7 relocated hibernate-jpamodelgen to hibernate-processor.
        processors = ["org.hibernate.orm:hibernate-processor"],
        processor_classes = ["org.hibernate.processor.HibernateProcessor"],
    ),
    "http": _feature(
        api = ["io.micronaut:micronaut-http"],
        processors = ["io.micronaut:micronaut-http-validation"],
        runtime = [],
    ),
    "http_client": _feature(
        includes = ["http"],
        # Applications choose their client implementation in runtime_deps.
        api = ["io.micronaut:micronaut-http-client-core"],
    ),
    "jakarta_el": _feature(
        api = [
            "io.micronaut.el:micronaut-jakarta-el-annotations",
            "io.micronaut.el:micronaut-jakarta-el",
            "jakarta.el:jakarta.el-api",
        ],
        generates_api = True,
        processors = ["io.micronaut.el:micronaut-jakarta-el-processor"],
    ),
    "jax_rs": _feature(
        includes = ["http"],
        api = ["jakarta.ws.rs:jakarta.ws.rs-api", "io.micronaut.jaxrs:micronaut-jaxrs-server"],
        processors = ["io.micronaut.jaxrs:micronaut-jaxrs-processor"],
    ),
    "json_schema": _feature(
        api = ["io.micronaut.jsonschema:micronaut-json-schema-annotations"],
        processors = ["io.micronaut.jsonschema:micronaut-json-schema-processor"],
    ),
    "kafka": _feature(
        # Kafka clients and listeners use core annotation processing and AOP.
        api = [
            "io.micronaut.kafka:micronaut-kafka",
            "io.micronaut:micronaut-messaging",
            "org.apache.kafka:kafka-clients",
        ],
    ),
    "langchain4j": _feature(
        api = [
            "io.micronaut.langchain4j:micronaut-langchain4j-core",
            "dev.langchain4j:langchain4j",
            "dev.langchain4j:langchain4j-core",
        ],
        processors = ["io.micronaut.langchain4j:micronaut-langchain4j-processor"],
    ),
    "lombok": _feature(
        # Processing is opt-in through @micronaut//:lombok_processor in plugins,
        # so it runs before Micronaut's exported processors.
        compile_only = ["org.projectlombok:lombok"],
    ),
    "micrometer_annotation": _feature(
        api = ["io.micrometer:micrometer-core", "io.micronaut.micrometer:micronaut-micrometer-core"],
        processors = ["io.micronaut.micrometer:micronaut-micrometer-annotation"],
    ),
    "multi_tenancy": _feature(
        api = [
            "io.micronaut.multitenancy:micronaut-multitenancy",
            # Expression compilation inspects TenantEvaluationContext's logger field.
            "org.slf4j:slf4j-api",
        ],
        processors = ["io.micronaut.multitenancy:micronaut-multitenancy-annotations"],
    ),
    "openapi": _feature(
        compile_only = [
            "io.micronaut.openapi:micronaut-openapi-annotations",
            "io.swagger.core.v3:swagger-annotations",
        ],
        processors = ["io.micronaut.openapi:micronaut-openapi"],
    ),
    "openapi_adoc": _feature(
        includes = ["openapi"],
        processors = ["io.micronaut.openapi:micronaut-openapi-adoc"],
    ),
    "opendi": _feature(
        api = [
            "org.eclipse.odi:micronaut-odi-cdi",
            "jakarta.enterprise:jakarta.enterprise.cdi-api",
        ],
        processors = ["org.eclipse.odi:micronaut-odi-processor-cdi"],
    ),
    "picocli": _feature(
        api = ["info.picocli:picocli", "io.micronaut.picocli:micronaut-picocli"],
        processors = ["info.picocli:picocli-codegen"],
        processor_classes = ["picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor"],
    ),
    "security": _feature(
        api = [
            "io.micronaut.security:micronaut-security",
            # @Secured and the security annotation APIs.
            "io.micronaut.security:micronaut-security-annotations",
        ],
        processors = ["io.micronaut.security:micronaut-security-processor"],
        runtime = [],
    ),
    "serialization": _feature(
        api = ["io.micronaut.serde:micronaut-serde-api"],
        processors = ["io.micronaut.serde:micronaut-serde-processor"],
    ),
    # Named bindings are opt-in runtime choices for applications and tests.
    "serialization_bson": _feature(
        includes = ["serialization"],
        api = ["org.mongodb:bson"],
        runtime = ["io.micronaut.serde:micronaut-serde-bson"],
    ),
    "serialization_jackson": _feature(
        includes = ["serialization"],
        runtime = ["io.micronaut.serde:micronaut-serde-jackson"],
    ),
    "serialization_jsonp": _feature(
        includes = ["serialization"],
        api = ["jakarta.json:jakarta.json-api"],
        # The upstream POM selects Parsson; applications can exclude it in
        # maven.install and supply their JSON-P provider in runtime_deps.
        runtime = ["io.micronaut.serde:micronaut-serde-jsonp"],
    ),
    "servlet": _feature(
        api = ["jakarta.servlet:jakarta.servlet-api"],
        processors = ["io.micronaut.servlet:micronaut-servlet-processor"],
    ),
    "sourcegen_generator": _feature(
        generates_api = True,
        processors = ["io.micronaut.sourcegen:micronaut-sourcegen-generator-java"],
    ),
    "spring": _feature(
        api = [
            "org.springframework:spring-context",
            "org.springframework:spring-beans",
            "org.springframework:spring-core",
        ],
        processors = ["io.micronaut.spring:micronaut-spring-annotation"],
    ),
    "spring_boot": _feature(
        includes = ["spring"],
        api = ["org.springframework.boot:spring-boot", "org.springframework.boot:spring-boot-autoconfigure"],
        processors = ["io.micronaut.spring:micronaut-spring-boot-annotation"],
        runtime = ["io.micronaut.spring:micronaut-spring-boot"],
    ),
    "spring_web": _feature(
        includes = ["spring"],
        api = [
            "org.springframework:spring-web",
            "io.micronaut:micronaut-http",
            "io.micronaut:micronaut-http-server",
        ],
        processors = ["io.micronaut.spring:micronaut-spring-web-annotation"],
        runtime = ["io.micronaut.spring:micronaut-spring-web"],
    ),
    "tracing_opentelemetry_annotations": _feature(
        api = [
            "io.opentelemetry:opentelemetry-api",
            "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations",
            "io.micronaut.tracing:micronaut-tracing-annotation",
        ],
        processors = ["io.micronaut.tracing:micronaut-tracing-opentelemetry-annotation"],
        runtime = ["io.micronaut.tracing:micronaut-tracing-opentelemetry"],
    ),
    "validation": _feature(
        api = [
            "io.micronaut.validation:micronaut-validation",
            "jakarta.validation:jakarta.validation-api",
        ],
        processors = ["io.micronaut.validation:micronaut-validation-processor"],
        runtime = [],
    ),
}

# Server runtimes selectable on applications and tests. Servlet-based source
# libraries select the servlet feature to process servlet injection points.
RUNTIMES = {
    "jetty": ["io.micronaut.servlet:micronaut-http-server-jetty"],
    "netty": ["io.micronaut:micronaut-http-server-netty"],
    "none": [],
    "tomcat": ["io.micronaut.servlet:micronaut-http-server-tomcat"],
    "undertow": ["io.micronaut.servlet:micronaut-http-server-undertow"],
}

TEST_JUNIT5 = struct(
    api = [
        "io.micronaut.test:micronaut-test-junit5",
        # @MockBean, @Property and other annotations shared by all test runtimes.
        "io.micronaut.test:micronaut-test-core",
        "org.junit.jupiter:junit-jupiter-api",
    ],
    runtime = [
        "org.junit.jupiter:junit-jupiter-engine",
        "org.junit.platform:junit-platform-launcher",
        "org.junit.platform:junit-platform-reporting",
    ],
)

def coordinate_to_target_name(coordinate):
    """Returns the rules_jvm_external target name for `group:artifact`.

    rules_jvm_external names targets by replacing every character that is not
    alphanumeric with an underscore, e.g. `io.micronaut:micronaut-inject`
    becomes `io_micronaut_micronaut_inject`.

    Args:
      coordinate: a versionless Maven coordinate, `group:artifact`.

    Returns:
      The target name within the rules_jvm_external repository.
    """
    parts = coordinate.split(":")
    if len(parts) != 2:
        fail("Expected a versionless `group:artifact` coordinate, got %r" % coordinate)
    return "".join([c if c.isalnum() else "_" for c in (parts[0] + "_" + parts[1]).elems()])
