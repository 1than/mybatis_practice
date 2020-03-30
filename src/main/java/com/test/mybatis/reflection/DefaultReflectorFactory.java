package com.test.mybatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultReflectorFactory implements ReflectorFactory {
	private boolean classCacheEnabled = true;
	private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<Class<?>, Reflector>();

	public DefaultReflectorFactory() {
	}

	public boolean isClassCacheEnabled() {
		return classCacheEnabled;
	}

	public void setClassCacheEnabled(boolean classCacheEnabled) {
		this.classCacheEnabled = classCacheEnabled;
	}

	public Reflector findForClass(Class<?> type) {
		if (classCacheEnabled) {
			// synchronized (type) removed see issue #461
			Reflector cached = reflectorMap.get(type);
			if (cached == null) {
				cached = new Reflector(type);
				reflectorMap.put(type, cached);
			}
			return cached;
		} else {
			return new Reflector(type);
		}
	}

}
