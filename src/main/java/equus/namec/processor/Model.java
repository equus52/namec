package equus.namec.processor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import equus.namec.GenerateName;

public class Model {
  private final ProcessingEnvironment processingEnv;
  private final TypeElement element;
  private final GenerateName annotation;

  public final LinkedHashSet<String> methodList = new LinkedHashSet<>();
  public final LinkedHashSet<String> propertyList = new LinkedHashSet<>();
  public final List<String> fieldList = new ArrayList<>();

  public final String generated = GenerateNameProcessor.class.getCanonicalName();

  public Model(ProcessingEnvironment processingEnv, TypeElement element) {
    this.processingEnv = processingEnv;
    this.element = element;
    this.annotation = element.getAnnotation(GenerateName.class);
  }

  public String getPackageName() {
    return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
  }

  public String getFullClassName() {
    return element.getQualifiedName().toString();
  }

  public String getSimpleClassName() {
    return element.getSimpleName().toString();
  }

  public String getGenerationClassName() {
    if (!annotation.name().isEmpty()) {
      return annotation.name();
    }
    return annotation.prefix() + getSimpleClassName() + annotation.suffix();
  }
}