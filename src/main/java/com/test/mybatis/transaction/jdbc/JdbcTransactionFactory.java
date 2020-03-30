package com.test.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.test.mybatis.session.TransactionIsolationLevel;
import com.test.mybatis.transaction.Transaction;
import com.test.mybatis.transaction.TransactionFactory;

public class JdbcTransactionFactory implements TransactionFactory {

	@Override
	public void setProperties(Properties props) {
	}

	@Override
	public Transaction newTransaction(Connection conn) {
		return new JdbcTransaction(conn);
	}

	@Override
	public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
		return new JdbcTransaction(ds, level, autoCommit);
	}
}
