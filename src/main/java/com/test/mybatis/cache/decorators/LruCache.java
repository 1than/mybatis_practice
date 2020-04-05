package com.test.mybatis.cache.decorators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import com.test.mybatis.cache.Cache;

/**
 * LRU，最近最少未使用缓存器，进行缓存清理在需要请求缓存的时候，会清除最近最少使用的缓存项
 * 
 * 
 * @author ethan
 *
 */
public class LruCache implements Cache {

	/**
	 * 
	 * 缓存对象
	 * 
	 */
	private final Cache delegate;

	/**
	 * 
	 * LinkedHashMap<Object, Object>类型对象，它是一个有序的HashMap，用于记录key最近的使用情况
	 * 
	 */
	private Map<Object, Object> keyMap;

	/**
	 * 
	 * 最少被使用的缓存项key
	 * 
	 */
	private Object eldestKey;

	public LruCache(Cache delegate) {
		this.delegate = delegate;
		setSize(2);// 默认缓存大小是1024
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	public void setSize(final int size) {// 重新设置缓存大小，会重制keyMap字段
		//构造函数的第三个参数，true表示该LinkedHashMap记录的顺序是access-order，
		//也就是说LinkedHashMap.get()方法会改变其记录的顺序
		keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
			private static final long serialVersionUID = 4267176411845948333L;
			//当调用LinkedHashMap.put()方法时，会调用该方法
			@Override
			protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
				System.out.println(eldest.getKey() + " --- " + eldest.getValue());
				boolean tooBig = size() > size;
				if (tooBig) {//如果已经达到缓存上限，则更新eldestKey字段，后面会删除该项
					eldestKey = eldest.getKey();
				}
				return tooBig;
			}
		};
	}

	@Override
	public void putObject(Object key, Object value) {
		delegate.putObject(key, value);
		cycleKeyList(key);
	}

	@Override
	public Object getObject(Object key) {
		keyMap.get(key); // 该key被访问过之后会被放在链表的后面
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyMap.clear();
	}

	@Override
	public ReadWriteLock getReadWriteLock() {
		return null;
	}

	private void cycleKeyList(Object key) {
		keyMap.put(key, key);
		if (eldestKey != null) {
			delegate.removeObject(eldestKey);
			eldestKey = null;
		}
	}
}
