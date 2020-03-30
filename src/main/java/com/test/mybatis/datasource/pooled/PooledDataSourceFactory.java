package com.test.mybatis.datasource.pooled;

import com.test.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

public class PooledDataSourceFactory extends UnpooledDataSourceFactory  {

	public PooledDataSourceFactory() {
		this.dataSource = new PooledDataSource();
	}
}
