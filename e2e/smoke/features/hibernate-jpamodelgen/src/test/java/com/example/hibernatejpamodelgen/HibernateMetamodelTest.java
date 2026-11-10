package com.example.hibernatejpamodelgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import org.junit.jupiter.api.Test;

class HibernateMetamodelTest {
  @Test
  void consumesGeneratedMetamodelFromAnotherLibrary() throws Exception {
    assertEquals("title", Book_.TITLE);
    assertEquals("id", Book_.ID);
    assertEquals(Book.class, Book_.class.getAnnotation(StaticMetamodel.class).value());
    assertEquals(SingularAttribute.class, Book_.class.getField("title").getType());
  }

  @Test
  void excludesProcessorFromRuntime() {
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("org.hibernate.processor.HibernateProcessor"));
  }
}
