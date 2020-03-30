package com.test.mybatis.mapping;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

public interface DatabaseIdProvider {

	default void setProperties(Properties p) {
		// NOP
	}

	String getDatabaseId(DataSource dataSource) throws SQLException;
}
