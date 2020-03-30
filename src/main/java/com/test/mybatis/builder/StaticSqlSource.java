package com.test.mybatis.builder;

import java.util.List;

import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.ParameterMapping;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.session.Configuration;


public class StaticSqlSource implements SqlSource {

	private final String sql;
	  private final List<ParameterMapping> parameterMappings;
	  private final Configuration configuration;

	  public StaticSqlSource(Configuration configuration, String sql) {
	    this(configuration, sql, null);
	  }

	  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
	    this.sql = sql;
	    this.parameterMappings = parameterMappings;
	    this.configuration = configuration;
	  }

	  @Override
	  public BoundSql getBoundSql(Object parameterObject) {
	    return new BoundSql(configuration, sql, parameterMappings, parameterObject);
	  }
}
