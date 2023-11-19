package com.marinos.xyz.budgetmanagement.core.mappers;

import com.marinos.xyz.budgetmanagement.core.mappers.dto.EntityDTO;
import com.marinos.xyz.budgetmanagement.core.mappers.dto.ReferenceDTO;
import com.marinos.xyz.budgetmanagement.core.mappers.dto.RequestDetailsDTO;
import com.marinos.xyz.budgetmanagement.core.mappers.dto.RequestListElementDTO;
import com.marinos.xyz.budgetmanagement.core.mappers.dto.ResponseDetailsDTO;
import com.marinos.xyz.budgetmanagement.core.mappers.dto.ResponseListElementDTO;
import java.beans.IntrospectionException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DefaultEntityMapper<T> {
  @Autowired
  BeanFactory beanFactory;

  protected <R extends EntityDTO<T>> ReflectionMapper<T, R> fromReflection(Class<R> targetClass) {
    //    Class<T> originClass = (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(),ReflectionMapper.class)[0];

    Class<T> originClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    try {
      return new ReflectionMapper<>(originClass, targetClass);
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract Function<T, ? extends ResponseDetailsDTO<T>> responseDetailsMapper();

  protected abstract Function<T, ? extends ResponseListElementDTO<T>> responseListMapper();

  protected abstract Function<T, ? extends RequestDetailsDTO<T>> requestDetailsMapper();

  protected abstract Function<T, ? extends RequestListElementDTO<T>> requestListMapper();

  protected abstract Function<T, ? extends ReferenceDTO<T>> referenceMapper();

  public Class<T> forClass(){
    return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }
  public <U>DefaultEntityMapper<U> getMapperForEntity(Class<U>entityClass){
    return beanFactory.getBeanProvider(DefaultEntityMapper.class).stream().filter(m->m.forClass().equals(entityClass)).findFirst().orElseThrow();
  }
  public ResponseDetailsDTO<T> asResponse(T entity) {
    return responseDetailsMapper().apply(entity);
  }

  public List<? extends ResponseListElementDTO<T>> asResponse(Collection<T> entities) {
    return entities.stream().map(t -> responseListMapper().apply(t)).toList();
  }

  public RequestDetailsDTO<T> asRequest(T entity) {
    return requestDetailsMapper().apply(entity);
  }

  public List<? extends RequestListElementDTO<T>> asRequest(Collection<T> entities) {
    return entities.stream().map(t -> requestListMapper().apply(t)).toList();
  }

  public ReferenceDTO<T> asReference(T entity) {
    return referenceMapper().apply(entity);
  }
}
