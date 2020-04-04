package com.test.mybatis.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.test.mybatis.reflection.ArrayUtil;

/**
 * 
 *   唯一确定一个缓存项的key，mybatis因为涉及动态sql等多方面的因素，其缓存项key不能仅仅通过一个string表示，所以mybatis
 *   提供了CacheKey类来表示缓存项的key，在一个CacheKey对象中可以封装多个印象缓存项的因素。
 *   CacheKey中可以添加多个对象，由于这些对象共同确定两个CacheKey对象是否相同。
 *   
 *   以下四个部分构成CacheKey对象，也就是说以下四部分会记录到该CacheKey对象的updateList集合中
 *   1.MappedStatement id
 *   2.指定查询结果集的范围，也就是 RowBounds.offset 和 RowBounds.limit
 *   3.查询所使用的 SQL 语句，也就是 boundSql.getSql（）方法返回的 SQL 语句，其中可能包含“？”占位符。
 *   4.用户传递给上述sql语句的实际参数值
 * 
 * @author ethan
 *
 */
public class CacheKey implements Cloneable, Serializable {

	private static final long serialVersionUID = 1146682552656046210L;

	public static final CacheKey NULL_CACHE_KEY = new CacheKey() {
		@Override
		public void update(Object object) {
			throw new CacheException("Not allowed to update a null cache key instance.");
		}

		@Override
		public void updateAll(Object[] objects) {
			throw new CacheException("Not allowed to update a null cache key instance.");
		}
	};

	private static final int DEFAULT_MULTIPLIER = 37;
	private static final int DEFAULT_HASHCODE = 17;

	/**
	 * 
	 * 参与计算hashcode默认值是37
	 * 
	 */
	private final int multiplier;
	
	/**
	 * 
	 * CacheKey对象的hashcode，初始值是17
	 * 
	 */
	private int hashcode;
	
	/**
	 * 
	 * 校验和
	 * 
	 */
	private long checksum;
	
	/**
	 * 
	 * 集合个数
	 * 
	 */
	private int count;
	// 8/21/2017 - Sonarlint flags this as needing to be marked transient. While
	// true if content is not serializable, this is not always true and thus should
	// not be marked transient.
	/**
	 * 
	 * 由该集合中的所有对象共同决定两个CacheKey是否相同
	 * 
	 */
	private List<Object> updateList;

	public CacheKey() {
		this.hashcode = DEFAULT_HASHCODE;
		this.multiplier = DEFAULT_MULTIPLIER;
		this.count = 0;
		this.updateList = new ArrayList<Object>();
	}

	public CacheKey(Object[] objects) {
		this();
		updateAll(objects);
	}

	public int getUpdateCount() {
		return updateList.size();
	}

	/**
	 * 
	 * 向CacheKey的updateList中添加对象时，用的是update方法
	 * 
	 * @param object
	 */
	public void update(Object object) {
		int baseHashCode = object == null ? 1 : ArrayUtil.hashCode(object);

		count++;
		checksum += baseHashCode;
		baseHashCode *= count;

		hashcode = multiplier * hashcode + baseHashCode;

		updateList.add(object);
	}

	public void updateAll(Object[] objects) {
		for (Object o : objects) {
			update(o);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof CacheKey)) {
			return false;
		}

		final CacheKey cacheKey = (CacheKey) object;

		if (hashcode != cacheKey.hashcode) {
			return false;
		}
		if (checksum != cacheKey.checksum) {
			return false;
		}
		if (count != cacheKey.count) {
			return false;
		}

		for (int i = 0; i < updateList.size(); i++) {
			Object thisObject = updateList.get(i);
			Object thatObject = cacheKey.updateList.get(i);
			if (!ArrayUtil.equals(thisObject, thatObject)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public String toString() {
		StringJoiner returnValue = new StringJoiner(":");
		returnValue.add(String.valueOf(hashcode));
		returnValue.add(String.valueOf(checksum));
		updateList.stream().map(ArrayUtil::toString).forEach(returnValue::add);
		return returnValue.toString();
	}

	@Override
	public CacheKey clone() throws CloneNotSupportedException {
		CacheKey clonedCacheKey = (CacheKey) super.clone();
		clonedCacheKey.updateList = new ArrayList<Object>(updateList);
		return clonedCacheKey;
	}
}
