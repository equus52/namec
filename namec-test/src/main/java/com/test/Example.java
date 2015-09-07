package com.test;

import equus.namec.GenerateName;

@GenerateName
public class Example {

  private final String field1;

  public Example(String field1) {
    this.field1 = field1;
  }

  public String getField1() {
    return field1;
  }

  public boolean isEmpty() {
    return false;
  }
}