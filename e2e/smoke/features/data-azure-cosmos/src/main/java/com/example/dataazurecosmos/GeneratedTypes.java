package com.example.dataazurecosmos;

// Document IDs use introspection metadata; ordinary DTOs get generated serdes.
public final class GeneratedTypes {
  public static Class<?>[] types() {
    return new Class<?>[] {
      BookRepository$Intercepted.class,
      $Book$Introspection.class,
      SerdeBookTitleSerializer.class,
      SerdeBookTitleDeserializer.class
    };
  }
}
