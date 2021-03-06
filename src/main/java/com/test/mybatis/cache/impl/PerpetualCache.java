package com.test.mybatis.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import com.test.mybatis.cache.Cache;
import com.test.mybatis.cache.CacheException;

/**
 * 
 * Cache接口的实现类，但大部分都是装饰器，只有PerpetualCache提供了Cache的基本实现
 * 
 * @author ethan
 *
 */
public class PerpetualCache implements Cache {
	
	private String id;

	private Map<Object, Object> cache = new HashMap<Object, Object>();

	public PerpetualCache(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getSize() {
		return cache.size();
	}

	@Override
	public void putObject(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return cache.get(key);
	}

	@Override
	public Object removeObject(Object key) {
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public ReadWriteLock getReadWriteLock() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof Cache)) {
			return false;
		}

		Cache otherCache = (Cache) o;
		return getId().equals(otherCache.getId());
	}

	@Override
	public int hashCode() {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		return getId().hashCode();
	}
}
