package com.test.mybatis.executor.result;

import java.util.Map;

import com.test.mybatis.reflection.MetaObject;
import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.reflection.ReflectorFactory;
import com.test.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.test.mybatis.session.ResultContext;
import com.test.mybatis.session.ResultHandler;

public class DefaultMapResultHandler<K, V> implements ResultHandler<V> {
	private final Map<K, V> mappedResults;
	private final String mapKey;
	private final ObjectFactory objectFactory;
	private final ObjectWrapperFactory objectWrapperFactory;
	private final ReflectorFactory reflectorFactory;

	@SuppressWarnings("unchecked")
	public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory,
			ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
		this.objectFactory = objectFactory;
		this.objectWrapperFactory = objectWrapperFactory;
		this.reflectorFactory = reflectorFactory;
		this.mappedResults = objectFactory.create(Map.class);
		this.mapKey = mapKey;
	}

	@Override
	public void handleResult(ResultContext<? extends V> context) {
		final V value = context.getResultObject();
		final MetaObject mo = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
		// TODO is that assignment always true?
		final K key = (K) mo.getValue(mapKey);
		mappedResults.put(key, value);
	}

	public Map<K, V> getMappedResults() {
		return mappedResults;
	}
}
