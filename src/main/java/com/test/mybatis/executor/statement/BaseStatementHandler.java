package com.test.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.test.mybatis.executor.ErrorContext;
import com.test.mybatis.executor.Executor;
import com.test.mybatis.executor.ExecutorException;
import com.test.mybatis.executor.keygen.KeyGenerator;
import com.test.mybatis.executor.parameter.ParameterHandler;
import com.test.mybatis.executor.resultset.ResultSetHandler;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.session.Configuration;
import com.test.mybatis.session.ResultHandler;
import com.test.mybatis.session.RowBounds;
import com.test.mybatis.type.TypeHandlerRegistry;

public abstract class BaseStatementHandler implements StatementHandler {
	
	protected final Configuration configuration;
	protected final ObjectFactory objectFactory;
	protected final TypeHandlerRegistry typeHandlerRegistry;
	protected final ResultSetHandler resultSetHandler;
	protected final ParameterHandler parameterHandler;

	protected final Executor executor;
	protected final MappedStatement mappedStatement;
	protected final RowBounds rowBounds;

	protected BoundSql boundSql;

	protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject,
			RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		this.configuration = mappedStatement.getConfiguration();
		this.executor = executor;
		System.out.println(" mappedStatement = " + mappedStatement);
		this.mappedStatement = mappedStatement;
		this.rowBounds = rowBounds;

		this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
		this.objectFactory = configuration.getObjectFactory();

		if (boundSql == null) { // issue #435, get the key before calculating the statement
			generateKeys(parameterObject);
			boundSql = mappedStatement.getBoundSql(parameterObject);
		}

		this.boundSql = boundSql;

		this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
		this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds,
				parameterHandler, resultHandler, boundSql);
	}

	@Override
	public BoundSql getBoundSql() {
		return boundSql;
	}

	@Override
	public ParameterHandler getParameterHandler() {
		return parameterHandler;
	}

	@Override
	public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
		ErrorContext.instance().sql(boundSql.getSql());
		Statement statement = null;
		try {
			statement = instantiateStatement(connection);
			setStatementTimeout(statement, transactionTimeout);
			setFetchSize(statement);
			return statement;
		} catch (SQLException e) {
			closeStatement(statement);
			throw e;
		} catch (Exception e) {
			closeStatement(statement);
			throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
		}
	}

	protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

	protected void setStatementTimeout(Statement stmt, Integer transactionTimeout) throws SQLException {
		Integer queryTimeout = null;
		if (mappedStatement.getTimeout() != null) {
			queryTimeout = mappedStatement.getTimeout();
		} else if (configuration.getDefaultStatementTimeout() != null) {
			queryTimeout = configuration.getDefaultStatementTimeout();
		}
		if (queryTimeout != null) {
			stmt.setQueryTimeout(queryTimeout);
		}
		StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
	}

	protected void setFetchSize(Statement stmt) throws SQLException {
		Integer fetchSize = mappedStatement.getFetchSize();
		if (fetchSize != null) {
			stmt.setFetchSize(fetchSize);
			return;
		}
		Integer defaultFetchSize = configuration.getDefaultFetchSize();
		if (defaultFetchSize != null) {
			stmt.setFetchSize(defaultFetchSize);
		}
	}

	protected void closeStatement(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected void generateKeys(Object parameter) {
		KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
		ErrorContext.instance().store();
		keyGenerator.processBefore(executor, mappedStatement, null, parameter);
		ErrorContext.instance().recall();
	}
}
