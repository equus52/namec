package equus.namec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation generate a interface which has static string fields of description of the class annotated by this
 * annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateName {
  public static final String CLASS_NAME = "equus.namec.GenerateName";

  /**
   * Returns a class name of generated source code.<br>
   * If this is not empty, ignore prefix and suffix.
   * 
   * @return a class name of generated source code
   */
  String name() default "";

  /**
   * Returns a class name prefix of generated source code.
   * 
   * @return a class name prefix of generated source code
   */
  String prefix() default "";

  /**
   * Returns a class name suffix of generated source code.
   * 
   * @return a class name suffix of generated source code
   */
  String suffix() default "Name";
}