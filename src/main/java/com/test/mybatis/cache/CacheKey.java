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
 *   CacheKey中可以添加多个对象，由于这些对象共同确定两个个CacheKey对象是否相同。
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

	/**
	 * 
	 * 以下两个默认值的取值解释：
	 * 
	 * 17是质子数中一个"不大不小"的存在，如果使用的是一个如2的较小的质数，
	 * 那么得出的乘积会在一个很小的范围内，很容易造成哈希值的冲突，如果选择一个100以上
	 * 的质数，得出的哈希值会超出int的最大范围，这两种都不合适，而如果对超过
	 * 5000个英文单词进行hashcode运算，并使用31，33，37，39和41作为乘子，每个常数算出的
	 * hash值冲突数都小于7个，因此选用了17，37作为备选乘数
	 * 
	 */
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

	/**
	 * 
	 * 因为缓存中的key不再是简单的string，所以判断两个key是否相同从简单的string是否相同变成两个CachekKey对象是否相同
	 * 因此必须重新equals，引入新的对象相等规则来判断两个缓存的key(对象)是否相同
	 * 
	 */
	@Override
	public boolean equals(Object object) {
		
		//是否是同一个对象
		if (this == object) {
			return true;
		}
		
		//类型是否相同
		if (!(object instanceof CacheKey)) {
			return false;
		}

		final CacheKey cacheKey = (CacheKey) object;

		//hashcode 必须相同
		if (hashcode != cacheKey.hashcode) {
			return false;
		}
		//checksum 必须相同
		if (checksum != cacheKey.checksum) {
			return false;
		}
		
		//count 必须相同
		if (count != cacheKey.count) {
			return false;
		}

		//updateList 中的每一个元素必须相同
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
