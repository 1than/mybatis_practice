package com.test.mybatis.scripting.defaults;

import java.util.HashMap;

import com.test.mybatis.builder.SqlSourceBuilder;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.scripting.xmltags.DynamicContext;
import com.test.mybatis.scripting.xmltags.SqlNode;
import com.test.mybatis.session.Configuration;


public class RawSqlSource implements SqlSource {

	private final SqlSource sqlSource;

	public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
		this(configuration, getSql(configuration, rootSqlNode), parameterType);
	}

	public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
		SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
		Class<?> clazz = parameterType == null ? Object.class : parameterType;
		sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
	}

	private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
		DynamicContext context = new DynamicContext(configuration, null);
		rootSqlNode.apply(context);
		return context.getSql();
	}

	@Override
	public BoundSql getBoundSql(Object parameterObject) {
		return sqlSource.getBoundSql(parameterObject);
	}
}
