package com.test.mybatis.scripting;

import java.util.HashMap;
import java.util.Map;

public class LanguageDriverRegistry {

	private final Map<Class<?>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<Class<?>, LanguageDriver>();

	private Class<?> defaultDriverClass = null;

	public void register(Class<?> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("null is not a valid Language Driver");
		}
		LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(cls);
		if (driver == null) {
			try {
				driver = (LanguageDriver) cls.newInstance();
				LANGUAGE_DRIVER_MAP.put(cls, driver);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
			}
		}
	}

	public void register(LanguageDriver instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null is not a valid Language Driver");
		}
		LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(instance.getClass());
		if (driver == null) {
			LANGUAGE_DRIVER_MAP.put(instance.getClass(), driver);
		}
	}

	public LanguageDriver getDriver(Class<?> cls) {
		return LANGUAGE_DRIVER_MAP.get(cls);
	}

	public LanguageDriver getDefaultDriver() {
		return getDriver(getDefaultDriverClass());
	}

	public Class<?> getDefaultDriverClass() {
		return defaultDriverClass;
	}

	public void setDefaultDriverClass(Class<?> defaultDriverClass) {
		register(defaultDriverClass);
		this.defaultDriverClass = defaultDriverClass;
	}
}
