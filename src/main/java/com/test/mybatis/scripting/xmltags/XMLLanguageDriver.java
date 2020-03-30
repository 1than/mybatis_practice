package com.test.mybatis.scripting.xmltags;

import com.test.mybatis.builder.xml.XMLMapperEntityResolver;
import com.test.mybatis.executor.parameter.ParameterHandler;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.mapping.SqlSource;
import com.test.mybatis.parsing.PropertyParser;
import com.test.mybatis.parsing.XNode;
import com.test.mybatis.parsing.XPathParser;
import com.test.mybatis.scripting.LanguageDriver;
import com.test.mybatis.scripting.defaults.DefaultParameterHandler;
import com.test.mybatis.scripting.defaults.RawSqlSource;
import com.test.mybatis.session.Configuration;

public class XMLLanguageDriver implements LanguageDriver {

	@Override
	public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject,
			BoundSql boundSql) {
		return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
		XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
		return builder.parseScriptNode();
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		// issue #3
		if (script.startsWith("<script>")) {
			XPathParser parser = new XPathParser(script, false, configuration.getVariables(),
					new XMLMapperEntityResolver());
			return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
		} else {
			// issue #127
			script = PropertyParser.parse(script, configuration.getVariables());
			TextSqlNode textSqlNode = new TextSqlNode(script);
			if (textSqlNode.isDynamic()) {
				return new DynamicSqlSource(configuration, textSqlNode);
			} else {
				return new RawSqlSource(configuration, script, parameterType);
			}
		}
	}
}
