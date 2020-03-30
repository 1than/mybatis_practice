package com.test.mybatis.scripting.defaults;

import com.test.mybatis.builder.BuilderException;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.parsing.XNode;
import com.test.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.test.mybatis.session.Configuration;

public class RawLanguageDriver extends XMLLanguageDriver {

	@Override
	public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
		SqlSource source = super.createSqlSource(configuration, script, parameterType);
		checkIsNotDynamic(source);
		return source;
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		SqlSource source = super.createSqlSource(configuration, script, parameterType);
		checkIsNotDynamic(source);
		return source;
	}

	private void checkIsNotDynamic(SqlSource source) {
		if (!RawSqlSource.class.equals(source.getClass())) {
			throw new BuilderException("Dynamic content is not allowed when using RAW language");
		}
	}
}
