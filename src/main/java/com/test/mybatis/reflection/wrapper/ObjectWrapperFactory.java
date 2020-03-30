package com.test.mybatis.reflection.wrapper;

import com.test.mybatis.reflection.MetaObject;

public interface ObjectWrapperFactory {
	boolean hasWrapperFor(Object object);

	ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
