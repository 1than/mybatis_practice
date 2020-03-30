package com.test.mybatis.scripting;

import com.test.mybatis.executor.parameter.ParameterHandler;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.parsing.XNode;
import com.test.mybatis.session.Configuration;

public interface LanguageDriver {
	
	ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

	SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

	SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);
}
