package com.test.mybatis.executor.resultset;

import java.util.ArrayList;
import java.util.List;

import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.session.ResultContext;
import com.test.mybatis.session.ResultHandler;

public class DefaultResultHandler implements ResultHandler<Object> {

	private final List<Object> list;

	public DefaultResultHandler() {
		list = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public DefaultResultHandler(ObjectFactory objectFactory) {
		list = objectFactory.create(List.class);
	}

	@Override
	public void handleResult(ResultContext<?> context) {
		list.add(context.getResultObject());
	}

	public List<Object> getResultList() {
		return list;
	}
}
