package com.example.datajpametamodel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.lang.reflect.ParameterizedType;
import org.junit.jupiter.api.Test;

class JpaMetamodelTest {
  @Test
  void consumesGeneratedMetamodelFromAnotherLibrary() throws Exception {
    assertEquals("id", Book_.ID);
    assertEquals("title", Book_.TITLE);
    assertEquals(Book.class, Book_.class.getAnnotation(StaticMetamodel.class).value());

    var id = (ParameterizedType) Book_.class.getField("id").getGenericType();
    assertEquals(SingularAttribute.class, id.getRawType());
    assertArrayEquals(new Class<?>[] {Book.class, Long.class}, id.getActualTypeArguments());

    var title = (ParameterizedType) Book_.class.getField("title").getGenericType();
    assertEquals(SingularAttribute.class, title.getRawType());
    assertArrayEquals(new Class<?>[] {Book.class, String.class}, title.getActualTypeArguments());
  }
}
