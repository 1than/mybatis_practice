package com.test.mybatis.reflection;

import java.lang.reflect.Type;

public interface ParameterizedType {

	Type[] getActualTypeArguments();
	
	Type getRawType();
	
	Type getOwnerType();
}
