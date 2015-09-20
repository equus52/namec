package equus.namec.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

import equus.namec.GenerateName;

/**
 * GenerateName generate a interface which has static string fields of description of the class annotated by
 * GenerateName.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(GenerateName.CLASS_NAME)
public class GenerateNameProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(GenerateName.class).forEach(
        element -> {
          Model model = createModel((TypeElement) element);
          String javaBody = createJavaBody(model);
          try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(
                model.getPackageName() + "." + model.getGenerationClassName(), element);
            try (BufferedWriter writer = new BufferedWriter(file.openWriter())) {
              writer.write(javaBody);
              writer.flush();
            }
          } catch (IOException e) {
            String trace = getStackTraceAsString(e);
            processingEnv.getMessager().printMessage(Kind.ERROR, trace, element);
          }
        });
    return true;
  }

  private Model createModel(TypeElement element) {
    Model model = new Model(processingEnv, element);

    List<VariableElement> fieldList = ElementFilter.fieldsIn(element.getEnclosedElements());
    List<String> fieldNameList = fieldList.stream().map(field -> field.getSimpleName().toString())
        .collect(Collectors.toList());
    model.fieldList.addAll(fieldNameList);
    model.propertyList.addAll(fieldNameList);

    List<ExecutableElement> methodList = ElementFilter.methodsIn(element.getEnclosedElements());
    List<String> methodNameList = methodList.stream().map(method -> getMethodName(method)).collect(Collectors.toList());
    model.methodList.addAll(methodNameList);

    List<String> propertyNameList = methodList.stream().filter(method -> isProperty(method))
        .map(method -> getPropertyName(method)).collect(Collectors.toList());
    model.propertyList.addAll(propertyNameList);

    return model;
  }

  private boolean isProperty(ExecutableElement method) {
    Set<Modifier> modifiers = method.getModifiers();
    if (modifiers.contains(Modifier.STATIC)) {
      return false;
    }
    return modifiers.contains(Modifier.PUBLIC) && isGetter(method);
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

  private String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private String createJavaBody(Model model) {
    String text = Resources.loadText(this, "name.template");
    Template template = new Template(text);
    return template.apply(model);
  }
}