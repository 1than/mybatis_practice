package com.test.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.test.mybatis.reflection.ReflectionException;


public class AmbiguousMethodInvoker extends MethodInvoker {
	
	private final String exceptionMessage;

	  public AmbiguousMethodInvoker(Method method, String exceptionMessage) {
	    super(method);
	    this.exceptionMessage = exceptionMessage;
	  }

	  @Override
	  public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
	    throw new ReflectionException(exceptionMessage);
	  }

}
