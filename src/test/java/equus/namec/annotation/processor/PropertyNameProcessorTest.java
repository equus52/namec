package equus.namec.annotation.processor;

import static com.google.common.truth.Truth.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.*;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

public class PropertyNameProcessorTest {

  @Test
  public void test_1() {
    JavaFileObject javaFile = JavaFileObjects.forSourceLines(//
        "test.Hoge",//

        "package test;",//
        "",//
        "import equus.namec.annotation.PropertyName;",//
        "",//
        "@PropertyName",//
        "public class Hoge {",//
        "",//
        "  private String sample1;",//
        "  public String getSample2(){return null;}",//
        "  public boolean isSample3(){return false;}",//
        "",//
        "}"//
    );
    JavaFileObject expectedOutput = JavaFileObjects.forSourceLines(//
        "test.Hoge_",//

        "package test;",//
        "",//
        "public class Hoge_ {",//
        "",//
        "  public static final String sample1 = \"sample1\";",//
        "  public static final String sample2 = \"sample2\";",//
        "  public static final String sample3 = \"sample3\";",//
        "",//
        "}"//
    );

    assert_().about(javaSource())//
        .that(javaFile)//
        .processedWith(new PropertyNameProcessor())//
        .compilesWithoutError()//
        .and().generatesSources(expectedOutput);
  }

}