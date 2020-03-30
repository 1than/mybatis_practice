package com.test.mybatis.transaction.managed;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.test.mybatis.session.TransactionIsolationLevel;
import com.test.mybatis.transaction.Transaction;
import com.test.mybatis.transaction.TransactionFactory;


public class ManagedTransactionFactory implements TransactionFactory{

	private boolean closeConnection = true;

	@Override
	public void setProperties(Properties props) {
		if (props != null) {
			String closeConnectionProperty = props.getProperty("closeConnection");
			if (closeConnectionProperty != null) {
				closeConnection = Boolean.parseBoolean(closeConnectionProperty);
			}
		}
	}

	@Override
	public Transaction newTransaction(Connection conn) {
		return new ManagedTransaction(conn, closeConnection);
	}

	@Override
	public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
		// Silently ignores autocommit and isolation level, as managed transactions are
		// entirely
		// controlled by an external manager. It's silently ignored so that
		// code remains portable between managed and unmanaged configurations.
		return new ManagedTransaction(ds, level, closeConnection);
	}
}
