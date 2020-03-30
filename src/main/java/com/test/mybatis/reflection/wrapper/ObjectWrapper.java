package com.test.mybatis.reflection.wrapper;

import java.util.List;

import com.test.mybatis.reflection.MetaObject;
import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.reflection.property.PropertyTokenizer;

public interface ObjectWrapper {
	
	Object get(PropertyTokenizer prop);

	void set(PropertyTokenizer prop, Object value);

	String findProperty(String name, boolean useCamelCaseMapping);

	String[] getGetterNames();

	String[] getSetterNames();

	Class<?> getSetterType(String name);

	Class<?> getGetterType(String name);

	boolean hasSetter(String name);

	boolean hasGetter(String name);

	MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

	boolean isCollection();

	public void add(Object element);

	public <E> void addAll(List<E> element);
}
