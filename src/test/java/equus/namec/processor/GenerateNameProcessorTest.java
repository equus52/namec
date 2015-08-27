package equus.namec.processor;

import static com.google.common.truth.Truth.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.*;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

public class GenerateNameProcessorTest {

  @Test
  public void test_1() {
    JavaFileObject javaFile = JavaFileObjects.forSourceLines(//
        "test.Hoge",//

        "package test;",//
        "",//
        "import equus.namec.GenerateName;",//
        "",//
        "@GenerateName",//
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
        "import javax.annotation.Generated;",//
        "",//
        "@Generated(\"equus.namec.processor.GenerateNameProcessor\")",//
        "public interface HogeName {",//
        "",//
        "  public static final String PACKAGE_test = \"test\";",//
        "",//
        "  public static final String CLASS_FULL_test_Hoge = \"test.Hoge\";",//
        "  public static final String CLASS_SIMPLE_Hoge = \"Hoge\";",//
        "",//
        "  public static final String FIELD_sample1 = \"sample1\";",//
        "",//
        "  public static final String METHOD_getSample2 = \"getSample2\";",//
        "  public static final String METHOD_isSample3 = \"isSample3\";",//
        "",//
        "  public static final String PROPERTY_sample1 = \"sample1\";",//
        "  public static final String PROPERTY_sample2 = \"sample2\";",//
        "  public static final String PROPERTY_sample3 = \"sample3\";",//
        "",//
        "}"//
    );

    assert_().about(javaSource())//
        .that(javaFile)//
        .processedWith(new GenerateNameProcessor())//
        .compilesWithoutError()//
        .and().generatesSources(expectedOutput);
  }

}