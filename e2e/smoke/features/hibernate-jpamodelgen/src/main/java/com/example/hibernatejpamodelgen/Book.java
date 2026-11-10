package com.example.hibernatejpamodelgen;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Book {
  @Id Long id;
  String title;
}
