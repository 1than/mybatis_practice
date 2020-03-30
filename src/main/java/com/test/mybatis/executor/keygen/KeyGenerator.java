package com.test.mybatis.executor.keygen;

import java.sql.Statement;

import com.test.mybatis.executor.Executor;
import com.test.mybatis.mapping.MappedStatement;

public interface KeyGenerator {

	void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

	void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
}
