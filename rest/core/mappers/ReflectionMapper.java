package com.marinos.xyz.budgetmanagement.core.mappers;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReflectionMapper<T, R>{

  private final Class<R> targetClass;
  private final Class<T> originClass;
  private final List<BiConsumer<T, R>> overrides = new ArrayList<>();

  public ReflectionMapper(Class<T> originClass, Class<R> targetClass) throws IntrospectionException {
    this.targetClass = targetClass;
    this.originClass = originClass;
  }

  public ReflectionMapper<T, R> override(BiConsumer<T, R> consumer) {
    overrides.add(consumer);
    return this;
  }

  public Function<T, R> get() {
    return origin -> {
      if(origin==null){
        return null;
      }
      final R target;
      try {
        target = targetClass.getConstructor().newInstance();
        for (PropertyDescriptor targetPd : Introspector.getBeanInfo(targetClass).getPropertyDescriptors()) {
          if ("class".equals(targetPd.getName())) {
            continue;
          }
          for (PropertyDescriptor originPd : Introspector.getBeanInfo(originClass).getPropertyDescriptors()) {
            if (originPd.getName().equals(targetPd.getName()) && originPd.getPropertyType().equals(targetPd.getPropertyType())) {
              targetPd.getWriteMethod().invoke(target, originPd.getReadMethod().invoke(origin));
            }
          }
        }
      } catch (Exception e) {
        System.out.println(origin);
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      overrides.forEach((o -> o.accept(origin, target)));
      return target;
    };

  }

}
