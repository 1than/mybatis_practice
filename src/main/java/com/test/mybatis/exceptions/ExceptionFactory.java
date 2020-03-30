package com.test.mybatis.exceptions;

import com.test.mybatis.executor.ErrorContext;

/**
 * 
 * 单例模式
 * 
 * @author ethan
 *
 */
public class ExceptionFactory {
	
	private ExceptionFactory() {
		// Prevent Instantiation
	}

	public static RuntimeException wrapException(String message, Exception e) {
		return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
	}
}
