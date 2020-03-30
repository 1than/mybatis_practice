package com.test.mybatis.executor.statement;

import java.sql.SQLException;
import java.sql.Statement;

public class StatementUtil {

	private StatementUtil() {
		// NOP
	}

	public static void applyTransactionTimeout(Statement statement, Integer queryTimeout, Integer transactionTimeout)
			throws SQLException {
		if (transactionTimeout == null) {
			return;
		}
		if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
			statement.setQueryTimeout(transactionTimeout);
		}
	}

}
