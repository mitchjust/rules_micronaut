package com.example.data;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NoPoolTest {
  @Test
  void repositoryFeaturesDoNotSelectAPoolOrDriver() {
    for (String implementation : new String[] {
      "com.zaxxer.hikari.HikariDataSource",
      "org.apache.tomcat.jdbc.pool.DataSource",
      "org.apache.commons.dbcp2.BasicDataSource",
      "org.h2.Driver",
      "org.postgresql.Driver"
    }) {
      assertThrows(ClassNotFoundException.class, () -> Class.forName(implementation));
    }
  }
}
