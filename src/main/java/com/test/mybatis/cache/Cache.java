package com.test.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * 
 * 缓存中的核心模块，定义了缓存的基本行为
 * 
 * @author ethan
 *
 */
public interface Cache {
	
	
	/**
	 * 缓存对象id
	 * 
	 * @return The identifier of this cache
	 */
	String getId();

	/**
	 * 
	 * 向缓存中添加数据，一般情况下，key是CacheKey，value是查询结果
	 * 
	 * @param key   Can be any object but usually it is a {@link CacheKey}
	 * @param value The result of a select.
	 */
	void putObject(Object key, Object value);

	/**
	 * 根据指定的key，在缓存中查找相应的结果
	 * 
	 * @param key The key
	 * @return The object stored in the cache.
	 */
	Object getObject(Object key);

	/**
	 * 
	 * 删除key对应的缓存选项
	 * 
	 * As of 3.3.0 this method is only called during a rollback for any previous
	 * value that was missing in the cache. This lets any blocking cache to release
	 * the lock that may have previously put on the key. A blocking cache puts a
	 * lock when a value is null and releases it when the value is back again. This
	 * way other threads will wait for the value to be available instead of hitting
	 * the database.
	 *
	 * 
	 * @param key The key
	 * @return Not used
	 */
	Object removeObject(Object key);

	/**
	 * 
	 * 清空缓存
	 * 
	 * Clears this cache instance
	 */
	void clear();

	/**
	 * 缓存项个数
	 * 
	 * Optional. This method is not called by the core.
	 * 
	 * @return The number of elements stored in the cache (not its capacity).
	 */
	int getSize();

	/**
	 * 获取读写锁，不会被mybatis核心代码使用，可以提供空实现
	 * 
	 * 
	 * Optional. As of 3.2.6 this method is no longer called by the core.
	 * 
	 * Any locking needed by the cache must be provided internally by the cache
	 * provider.
	 * 
	 * @return A ReadWriteLock
	 */
	default ReadWriteLock getReadWriteLock() {
		return null;
	}
}
