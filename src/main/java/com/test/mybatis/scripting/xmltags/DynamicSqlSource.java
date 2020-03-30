package com.test.mybatis.scripting.xmltags;

import java.util.Map;

import com.test.mybatis.builder.SqlSourceBuilder;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.session.Configuration;

public class DynamicSqlSource implements SqlSource {

	private Configuration configuration;
	  private SqlNode rootSqlNode;

	  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
	    this.configuration = configuration;
	    this.rootSqlNode = rootSqlNode;
	  }

	  @Override
	  public BoundSql getBoundSql(Object parameterObject) {
	    DynamicContext context = new DynamicContext(configuration, parameterObject);
	    rootSqlNode.apply(context);
	    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
	    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
	    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
	    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
	    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
	      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
	    }
	    return boundSql;
	  }
}
