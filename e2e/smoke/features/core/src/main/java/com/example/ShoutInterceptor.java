package com.example;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Singleton;

@Singleton
@InterceptorBean(Shout.class)
public class ShoutInterceptor implements MethodInterceptor<Object, Object> {
  @Override
  public Object intercept(MethodInvocationContext<Object, Object> context) {
    Object result = context.proceed();
    return result instanceof String s ? s.toUpperCase() : result;
  }
}
