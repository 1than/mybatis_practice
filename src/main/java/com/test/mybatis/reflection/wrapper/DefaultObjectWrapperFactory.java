package com.test.mybatis.reflection.wrapper;

import com.test.mybatis.reflection.MetaObject;
import com.test.mybatis.reflection.ReflectionException;

public class DefaultObjectWrapperFactory implements ObjectWrapperFactory  {

	public boolean hasWrapperFor(Object object) {
		return false;
	}

	public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
		throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
	}

}
