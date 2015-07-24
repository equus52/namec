package equus.namec.annotation.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import equus.namec.annotation.PropertyName;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(PropertyName.CLASS_NAME)
public class PropertyNameProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(PropertyName.class)) {
      Set<String> propertySet = new LinkedHashSet<>();

      for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
        System.out.println("field: " + field);
        Set<Modifier> modifiers = field.getModifiers();
        if (modifiers.contains(Modifier.STATIC)) {
          continue;
        }
        propertySet.add(field.getSimpleName().toString());
      }
      for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
        System.out.println("method: " + method);
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.STATIC)) {
          continue;
        }
        if (modifiers.contains(Modifier.PUBLIC) && isGetter(method)) {
          String propertyName = getPropertyName(method);
          propertySet.add(propertyName);
        }
      }

      String packageName = getPackageName(element);
      String className = toCreateClassName(element);
      String javaString = createJava(packageName, className, propertySet);

      try {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className, element);
        try (BufferedWriter writer = new BufferedWriter(file.openWriter())) {
          writer.write(javaString);
          writer.flush();
        }
      } catch (IOException e) {
        String trace = getStackTraceAsString(e);
        processingEnv.getMessager().printMessage(Kind.ERROR, trace, element);
      }
    }

    return true;
  }

  private boolean isGetter(ExecutableElement method) {
    if (!method.getParameters().isEmpty()) {
      return false;
    }
    String methodName = getMethodName(method);
    return methodName.startsWith("get") || methodName.startsWith("is");
  }

  private String getMethodName(ExecutableElement method) {
    return method.getSimpleName().toString();
  }

  private String getPropertyName(ExecutableElement method) {
    String methodName = getMethodName(method);
    String value = methodName.replaceAll("^(set|get|is)", "");
    return new StringBuilder(value.length())//
        .append(Character.toLowerCase(value.charAt(0)))//
        .append(value.substring(1)).toString();
  }

  public String toCreateClassName(Element element) {
    return getClassName(element) + "_";
  }

  public String getClassName(Element element) {
    return element.getSimpleName().toString();
  }

  public String getPackageName(Element element) {
    return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
  }

  public String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private String createJava(String packageName, String className, Set<String> propertySet) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("package %s;%n", packageName));
    builder.append("\n");
    builder.append(String.format("public class %s {%n", className));
    builder.append("\n");
    for (String propertyName : propertySet) {
      builder.append(String.format("  public static final String %s = \"%s\";%n", propertyName, propertyName));
    }
    builder.append("\n}");

    return builder.toString();
  }
}