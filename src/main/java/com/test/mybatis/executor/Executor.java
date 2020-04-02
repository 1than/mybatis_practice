package com.test.mybatis.executor;

import java.sql.SQLException;
import java.util.List;

import com.test.mybatis.cache.CacheKey;
import com.test.mybatis.cursor.Cursor;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.reflection.MetaObject;
import com.test.mybatis.session.ResultHandler;
import com.test.mybatis.session.RowBounds;
import com.test.mybatis.transaction.Transaction;


/**
 *  mybati的executor执行器，主要负责执行具体的sql语句
 * 
 * 1.读写操作相关方法
 * 2.事务相关方法
 * 3.缓存相关方法
 * 4.设置延迟加载方法
 * 5.设置包装executor的方法
 * 
 * Executor下主要有两个实现类，BaseExecutor(本地缓存), CachingExecutor(二级缓存)
 * 
 * @author ethan
 *
 */
public interface Executor {

	//空 ResultHandler 对象的枚举
	ResultHandler NO_RESULT_HANDLER = null;

	//插入/更新/删除，由传入的MappedStatement 的sql
	int update(MappedStatement ms, Object parameter) throws SQLException;

	//查询，带ResultHandler + CacheKey + BoundSql
	<E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
			CacheKey cacheKey, BoundSql boundSql) throws SQLException;

	//查询，带ResultHandler
	<E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
			throws SQLException;

	//查询，返回值为Cursor
	<E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

	//插入批处理语句
	List<BatchResult> flushStatements() throws SQLException;

	//提交事务
	void commit(boolean required) throws SQLException;

	//回滚事务
	void rollback(boolean required) throws SQLException;

	//创建CacheKey对象
	CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

	//判断是否是缓存
	boolean isCached(MappedStatement ms, CacheKey key);

	//清除本地缓存
	void clearLocalCache();

	//延迟加载
	void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

	//获得事务
	Transaction getTransaction();

	//关闭事务
	void close(boolean forceRollback);

	//判断事务是否关闭
	boolean isClosed();

	//设置包装的Executor对象
	void setExecutorWrapper(Executor executor);
}
