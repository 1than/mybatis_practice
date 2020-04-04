package com.test.mybatis.cache.decorators;

import java.util.Deque;
import java.util.LinkedList;

import com.test.mybatis.cache.Cache;

/**
 * 先进先出缓存器，有时候为了控制缓存的大小，系统需要按照一定的规则清理缓存，FifoCache是先进先出版本的装饰器
 * 当向缓存中添加数据时，如果缓存项的个数已经达到上限，则会将缓存中最老(最先进入缓存)的缓存删除
 * 
 * @author ethan
 *
 */
public class FifoCache implements Cache {

	/**
	 * 
	 * 缓存对象
	 * 
	 */
	private final Cache delegate;
	
	/**
	 * 
	 * 用于记录key进入缓存的先后顺序，使用的是LinkedList<Object>类型的集合对象
	 * 
	 */
	private final Deque<Object> keyList;
	
	/**
	 * 
	 * 记录了缓存的上限，超过该值，则需要清理最老的缓存项
	 * 
	 */
	private int size;

	public FifoCache(Cache delegate) {
		this.delegate = delegate;
		this.keyList = new LinkedList<>();
		this.size = 1024;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public void putObject(Object key, Object value) {
		cycleKeyList(key);//检测并清理缓存
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyList.clear();
	}

	private void cycleKeyList(Object key) {
		keyList.addLast(key);//从队列尾部添加缓存key
		if (keyList.size() > size) {//从队列头部删除先进队列的key
			Object oldestKey = keyList.removeFirst();
			delegate.removeObject(oldestKey);
		}
	}

}
