package equus.namec.processor;

import static com.google.common.truth.Truth.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.*;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

public class GenerateNameProcessorTest {

  @Test
  public void test_1() {
    Lines inputLines = new Lines( //
        "package com.test;",//
        "",//
        "import equus.namec.GenerateName;",//
        "",//
        "@GenerateName",//
        "public class Example {",//
        "",//
        "  private final String field1 = \"test\";",//
        "",//
        "  public String getField1(){",//
        "    return field1;",//
        "  }",//
        "",//
        "  public boolean isEmpty(){",//
        "    return false;",//
        "  }",//
        "}"//
    );
    JavaFileObject javaFile = JavaFileObjects.forSourceString("com.test.Example", inputLines.toString());
    System.out.println("input:");
    System.out.println(inputLines);

    Lines expectedLines = new Lines( //
        "package com.test;",//
        "",//
        "import javax.annotation.Generated;",//
        "",//
        "@Generated(\"equus.namec.processor.GenerateNameProcessor\")",//
        "public interface ExampleName {",//
        "",//
        "  static String CLASS_CANONICAL = \"com.test.Example\";",//
        "  static String CLASS_SIMPLE = \"Example\";",//
        "",//
        "  static String PACKAGE = \"com.test\";",//
        "",//
        "  static String FIELD_field1 = \"field1\";",//
        "",//
        "  static String METHOD_getField1 = \"getField1\";",//
        "  static String METHOD_isEmpty = \"isEmpty\";",//
        "",//
        "  static String PROPERTY_field1 = \"field1\";",//
        "  static String PROPERTY_empty = \"empty\";",//
        "",//
        "}"//
    );
    JavaFileObject expectedOutput = JavaFileObjects.forSourceString("com.test.ExampleName", expectedLines.toString());
    System.out.println("expected:");
    System.out.println(expectedLines);

    assert_().about(javaSource())//
        .that(javaFile)//
        .processedWith(new GenerateNameProcessor())//
        .compilesWithoutError()//
        .and().generatesSources(expectedOutput);
  }

  static class Lines {
    final String[] lines;

    Lines(String... lines) {
      this.lines = lines;
    }

    @Override
    public String toString() {
      return String.join(System.lineSeparator(), lines);
    }
  }

}