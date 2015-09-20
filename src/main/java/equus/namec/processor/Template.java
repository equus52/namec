package equus.namec.processor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple template like Handlebars.
 */
public class Template {

  private static final Pattern VARIABLE = Pattern.compile("\\{\\{(.*?)\\}\\}");
  private static final Pattern COLLECTION = Pattern.compile("\\{\\{#each (.*?)\\}\\}(.*?)\\{\\{/each\\}\\}",
      Pattern.DOTALL);
  private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";

  private final String template;

  public Template(String template) {
    this.template = template;
  }

  public String apply(Object context) {
    return apply(template, new Context(context));
  }

  private String apply(String template, Context context) {
    String convertedTemplate = convertCollection(template, context);
    return convertVariable(convertedTemplate, context);
  }

  private String convertCollection(String template, Context context) {
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = COLLECTION.matcher(template);
    while (matcher.find()) {
      String key = matcher.group(1);
      String body = matcher.group(2);
      matcher.appendReplacement(buffer, expandCollection(context, key, body));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private String expandCollection(Context context, String key, String body) {
    Object list = context.get(key);
    if (list == null) {
      return "";
    }
    if (!Iterable.class.isAssignableFrom(list.getClass())) {
      return "";
    }
    String trimedBody = trimLineSeparator(body);
    Stream<?> stream = StreamSupport.stream(((Iterable<?>) list).spliterator(), false);
    return stream.map(e -> apply(trimedBody, new Context(e))).collect(Collectors.joining(System.lineSeparator()));
  }

  private String trimLineSeparator(String body) {
    body = body.replaceAll("^" + LINE_SEPARATOR_PATTERN, "");
    body = body.replaceAll(LINE_SEPARATOR_PATTERN + "$", "");
    return body;
  }

  private String convertVariable(String template, Context context) {
    Matcher matcher = VARIABLE.matcher(template);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      String match = matcher.group(1);
      matcher.appendReplacement(buffer, context.get(match).toString());
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private static class Context {

    private final Object bean;

    public Context(Object model) {
      this.bean = model;
    }

    public Object get(String key) {
      return get(bean, key);
    }

    private Object get(Object bean, String key) {
      if ("this".equals(key)) {
        return bean;
      }
      int p = key.indexOf('.');
      if (p > 0) {
        Object property = get(bean, key.substring(0, p));
        if (property == null) {
          return null;
        }
        return get(property, key.substring(p + 1));
      }
      Object property = getProperty(bean, key);
      return property == null ? "" : property;
    }

    private Object getProperty(Object obj, String propertyName) {
      Class<?> clazz = bean.getClass();
      List<BeanInfo> beanInfoList = getBeanInfo(clazz);
      for (BeanInfo beanInfo : beanInfoList) {
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
          if (propertyName.equals(propertyDescriptor.getName())) {
            Method method = propertyDescriptor.getReadMethod();
            if (method == null) {
              continue;
            }
            try {
              return method.invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
      try {
        Field field = clazz.getField(propertyName);
        return field.get(obj);
      } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(String.format("property not found. [propertyName: %s]", propertyName), e);
      }
    }

    private List<BeanInfo> getBeanInfo(Class<?> clazz) {
      try {
        List<BeanInfo> list = new ArrayList<>();
        list.add(Introspector.getBeanInfo(clazz));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
          list.addAll(getBeanInfo(superClass));
        }
        for (Class<?> i : clazz.getInterfaces()) {
          list.addAll(getBeanInfo(i));
        }
        return list;
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
