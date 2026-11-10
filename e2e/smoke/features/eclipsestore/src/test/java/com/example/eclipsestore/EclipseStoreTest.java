package com.example.eclipsestore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.ApplicationContext;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EclipseStoreTest {
  @TempDir Path directory;

  @Test
  void persistsAnnotatedChangesAcrossContextRestarts() {
    var configuration =
        Map.<String, Object>of(
            "eclipsestore.storage.default.root-class", Root.class.getName(),
            "eclipsestore.storage.default.storage-directory", directory.toString(),
            "eclipsestore.storage.default.channel-count", 1);
    // Persistence must survive closing this context and starting a fresh one.
    try (var context = ApplicationContext.run(configuration)) {
      context.getBean(Messages.class).write("persisted");
    }
    try (var context = ApplicationContext.run(configuration)) {
      assertEquals("persisted", context.getBean(Messages.class).read());
    }
  }
}
