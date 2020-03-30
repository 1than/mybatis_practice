package com.test.mybatis.mapping;

public interface SqlSource {
	
	BoundSql getBoundSql(Object parameterObject);
}
