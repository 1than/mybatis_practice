package com.test.mybatis.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.test.mybatis.reflection.ExceptionUtil;
import com.test.mybatis.session.SqlSession;

/**
 * 继承InvocationHandler 用于在执行真实对象前后做一些其他业务逻辑
 * @author ethan
 *
 * @param <T>
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			try {
				return method.invoke(this, args);
			} catch (Throwable t) {
				throw ExceptionUtil.unwrapThrowable(t);
			}
		}
		final MapperMethod mapperMethod = cachedMapperMethod(method);
		return mapperMethod.execute(sqlSession, args);
	}

	private static final long serialVersionUID = -6424540398559729838L;
	private final SqlSession sqlSession;
	private final Class<T> mapperInterface;
	private final Map<Method, MapperMethod> methodCache;

	public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
		this.sqlSession = sqlSession;
		this.mapperInterface = mapperInterface;
		this.methodCache = methodCache;
	}

	private MapperMethod cachedMapperMethod(Method method) {
		MapperMethod mapperMethod = methodCache.get(method);
		if (mapperMethod == null) {
			mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
			methodCache.put(method, mapperMethod);
		}
		return mapperMethod;
	}

}
