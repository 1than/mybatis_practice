package com.test.mybatis.session;

public interface ResultHandler<T> {
	
	void handleResult(ResultContext<? extends T> resultContext);
}
