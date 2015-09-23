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

public class BeanAccessor {

  private final Object bean;

  public BeanAccessor(Object bean) {
    this.bean = bean;
  }

  public Object get(String key) {
    if ("this".equals(key)) {
      return bean;
    }
    int p = key.indexOf('.');
    if (p > 0) {
      Object property = new BeanAccessor(bean).get(key.substring(0, p));
      if (property == null) {
        return null;
      }
      return new BeanAccessor(property).get(key.substring(p + 1));
    }
    Object property = getProperty(key);
    return property == null ? "" : property;
  }

  private Object getProperty(String propertyName) {
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
            return method.invoke(bean);
          } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    try {
      Field field = clazz.getField(propertyName);
      return field.get(bean);
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
