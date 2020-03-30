package com.test.mybatis.executor.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.test.mybatis.cursor.Cursor;
import com.test.mybatis.executor.Executor;
import com.test.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.test.mybatis.executor.keygen.KeyGenerator;
import com.test.mybatis.mapping.BoundSql;
import com.test.mybatis.mapping.MappedStatement;
import com.test.mybatis.mapping.ResultSetType;
import com.test.mybatis.session.ResultHandler;
import com.test.mybatis.session.RowBounds;

public class PreparedStatementHandler extends BaseStatementHandler {

	public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter,
			RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
	}

	@Override
	public int update(Statement statement) throws SQLException {
		PreparedStatement ps = (PreparedStatement) statement;
		ps.execute();
		int rows = ps.getUpdateCount();
		Object parameterObject = boundSql.getParameterObject();
		KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
		keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
		return rows;
	}

	@Override
	public void batch(Statement statement) throws SQLException {
		PreparedStatement ps = (PreparedStatement) statement;
		ps.addBatch();
	}

	@Override
	public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
		PreparedStatement ps = (PreparedStatement) statement;
		ps.execute();
		return resultSetHandler.handleResultSets(ps);
	}

	@Override
	public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
		PreparedStatement ps = (PreparedStatement) statement;
		ps.execute();
		return resultSetHandler.handleCursorResultSets(ps);
	}

	@Override
	protected Statement instantiateStatement(Connection connection) throws SQLException {
		String sql = boundSql.getSql();
		if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
			String[] keyColumnNames = mappedStatement.getKeyColumns();
			if (keyColumnNames == null) {
				return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			} else {
				return connection.prepareStatement(sql, keyColumnNames);
			}
		} else if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
			return connection.prepareStatement(sql);
		} else {
			System.out.println(mappedStatement.getResultSetType() == null);
			return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(),
					ResultSet.CONCUR_READ_ONLY);
		}
	}

	@Override
	public void parameterize(Statement statement) throws SQLException {
		parameterHandler.setParameters((PreparedStatement) statement);
	}

}
