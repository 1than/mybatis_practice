package com.test.mybatis.cache.decorators;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.test.mybatis.cache.Cache;
import com.test.mybatis.cache.CacheException;


public class BlockingCache implements Cache {

	private long timeout;
	private final Cache delegate;
	private final ConcurrentHashMap<Object, ReentrantLock> locks;

	public BlockingCache(Cache delegate) {
		this.delegate = delegate;
		this.locks = new ConcurrentHashMap<>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	@Override
	public void putObject(Object key, Object value) {
		try {
			delegate.putObject(key, value);
		} finally {
			releaseLock(key);
		}
	}

	@Override
	public Object getObject(Object key) {
		acquireLock(key);
		Object value = delegate.getObject(key);
		if (value != null) {
			releaseLock(key);
		}
		return value;
	}

	@Override
	public Object removeObject(Object key) {
		// despite of its name, this method is called only to release locks
		releaseLock(key);
		return null;
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	private ReentrantLock getLockForKey(Object key) {
		return locks.computeIfAbsent(key, k -> new ReentrantLock());
	}

	private void acquireLock(Object key) {
		Lock lock = getLockForKey(key);
		if (timeout > 0) {
			try {
				boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
				if (!acquired) {
					throw new CacheException("Couldn't get a lock in " + timeout + " for the key " + key
							+ " at the cache " + delegate.getId());
				}
			} catch (InterruptedException e) {
				throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
			}
		} else {
			lock.lock();
		}
	}

	private void releaseLock(Object key) {
		ReentrantLock lock = locks.get(key);
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
		}
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
