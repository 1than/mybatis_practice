package com.test.mybatis.executor.loader;

import java.util.List;
import java.util.Properties;

import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.session.Configuration;

public interface ProxyFactory {

	default void setProperties(Properties properties) {
		// NOP
	}

	Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration,
			ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

}
