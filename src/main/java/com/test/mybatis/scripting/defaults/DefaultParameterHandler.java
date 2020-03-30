package com.test.mybatis.scripting.defaults;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.test.mybatis.executor.ErrorContext;
import com.test.mybatis.executor.parameter.ParameterHandler;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.mapping.ParameterMapping;
import com.test.mybatis.mapping.ParameterMode;
import com.test.mybatis.reflection.MetaObject;
import com.test.mybatis.session.Configuration;
import com.test.mybatis.type.JdbcType;
import com.test.mybatis.type.TypeException;
import com.test.mybatis.type.TypeHandler;
import com.test.mybatis.type.TypeHandlerRegistry;

public class DefaultParameterHandler implements ParameterHandler {

	private final TypeHandlerRegistry typeHandlerRegistry;

	private final MappedStatement mappedStatement;
	private final Object parameterObject;
	private final BoundSql boundSql;
	private final Configuration configuration;

	public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
		this.mappedStatement = mappedStatement;
		this.configuration = mappedStatement.getConfiguration();
		this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		this.parameterObject = parameterObject;
		this.boundSql = boundSql;
	}

	@Override
	public Object getParameterObject() {
		return parameterObject;
	}

	@Override
	public void setParameters(PreparedStatement ps) {
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else {
						MetaObject metaObject = configuration.newMetaObject(parameterObject);
						value = metaObject.getValue(propertyName);
					}
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					JdbcType jdbcType = parameterMapping.getJdbcType();
					if (value == null && jdbcType == null) {
						jdbcType = configuration.getJdbcTypeForNull();
					}
					try {
						typeHandler.setParameter(ps, i + 1, value, jdbcType);
					} catch (TypeException | SQLException e) {
						throw new TypeException(
								"Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
					}
				}
			}
		}
	}
}
