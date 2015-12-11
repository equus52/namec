package equus.namec.processor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Charsets;

public class TemplateTest {

  @Test
  public void test() throws Exception {

    Template template = new Template(load("name.template"));
    Context context = createContext();
    String result = template.apply(context);
    System.out.println(result);

    assertEquals(result, load("name.expected"));
  }

  private String load(String fileName) {
    try {
      URL resource = this.getClass().getResource(fileName);
      List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), Charsets.UTF_8);
      return String.join(System.lineSeparator(), lines);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private Context createContext() {
    Context context = new Context();
    context.generated = this.getClass().getCanonicalName();
    context.generationClassName = this.getClass().getCanonicalName();
    context.packageName = "cpm.hoge";
    context.fullClassName = "com.hoge.HogeName";
    context.simpleClassName = "HogeName";
    context.fieldList.add("sample1");
    context.methodList.add("getSample2");
    context.methodList.add("isSample3");
    context.propertyList.add("sample1");
    context.propertyList.add("sample2");
    context.propertyList.add("sample3");
    return context;
  }

  public static class Context {

    public String generated;
    public String generationClassName;

    public String packageName;
    public String fullClassName;
    public String simpleClassName;

    public Class<?> sourceClass;

    public List<String> fieldList = new ArrayList<>();
    public List<String> methodList = new ArrayList<>();
    public List<String> propertyList = new ArrayList<>();
  }

}
