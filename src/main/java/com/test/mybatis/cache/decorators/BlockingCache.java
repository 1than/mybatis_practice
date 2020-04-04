package com.test.mybatis.cache.decorators;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.test.mybatis.cache.Cache;
import com.test.mybatis.cache.CacheException;

/**
 * 
 * 阻塞版缓存器，保证只有一个线程到数据库查找指定key对应的数据
 * 
 * @author ethan
 *
 */
public class BlockingCache implements Cache {

	/**
	 * 阻塞超时时长
	 * 
	 */
	private long timeout;
	
	/**
	 * Cache对象
	 * 
	 */
	private final Cache delegate;
	
	/**
	 * 每个key都对应一个ReentrantLock对象
	 * 
	 */
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

	/**
	 * 
	 * 假设线程A在BlockingCache中未找到KeyA对应的缓存项时，线程A会获取keyA对应的锁，
	 * 后续线程在查找keyA时会发生阻塞
	 * 
	 */
	@Override
	public Object getObject(Object key) {
		acquireLock(key);//获取key对应的锁
		Object value = delegate.getObject(key);//查询key
		if (value != null) {//缓存有key对应的缓存项	，释放锁，否则继续持有锁
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

	/**
	 * 
	 * 获取指定key对应的锁，如果该key没有对应的锁对象则创建新的ReentrantLock对象，再加锁，
	 * 如果获取锁失败，则阻塞一段时间
	 * 
	 * @param key
	 */
	private void acquireLock(Object key) {
		Lock lock = getLockForKey(key);//获取ReentrantLock对象
		if (timeout > 0) {//获取锁，带超时时长
			try {
				boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
				if (!acquired) {// 超时，抛出异常
					throw new CacheException("Couldn't get a lock in " + timeout + " for the key " + key
							+ " at the cache " + delegate.getId());
				}
			} catch (InterruptedException e) {
				throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
			}
		} else {//获取锁，不带超时时长
			lock.lock();
		}
	}

	/**
	 * 
	 * 释放锁
	 * 
	 * @param key
	 */
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
