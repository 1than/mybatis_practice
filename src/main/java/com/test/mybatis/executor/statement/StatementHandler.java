package com.test.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.test.mybatis.cursor.Cursor;
import com.test.mybatis.executor.parameter.ParameterHandler;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.session.ResultHandler;

public interface StatementHandler {

	Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException;

	void parameterize(Statement statement) throws SQLException;

	void batch(Statement statement) throws SQLException;

	int update(Statement statement) throws SQLException;

	<E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

	<E> Cursor<E> queryCursor(Statement statement) throws SQLException;

	BoundSql getBoundSql();

	ParameterHandler getParameterHandler();
}
