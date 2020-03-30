package com.test.mybatis.mapping;

import javax.sql.DataSource;

import com.test.mybatis.transaction.TransactionFactory;

/**
 * 
 * mybatis Configuration下的Environment
 * 
 * 标准配置如下：
 *    <environment id="development">
 *       <transactionManager type="JDBC"/>
 *        <dataSource type="POOLED">
 *            <property name="driver" value="com.mysql.jdbc.Driver"/>
 *            <property name="url" value="jdbc:mysql://localhost:3306/mbtest"/>
 *            <property name="username" value="root"/>
 *             <property name="password" value="123456"/>
 *         </dataSource>
 *     </environment>
 *     
 *   可以根据id配置不同环境下的数据库，比如开发环境的数据库，测试环境数据库，生产环境数据库
 *   
 *   该类被final修饰表示对象一旦构建（通过构建对象传入初始化参数），终身有效，不能随意修改，所以成员变量只有get方法而没有set方法
 * 
 * @author ethan
 *
 */
public final class Environment {
	
	//环境id
	private final String id;
	
	//事物工厂
	private final TransactionFactory transactionFactory;
	
	//数据源
	private final DataSource dataSource;
	
	
	public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
		if (id == null) {
		    throw new IllegalArgumentException("Parameter 'id' must not be null");
		}
		
	    if (transactionFactory == null) {
	        throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
	    }
	    
	    if (dataSource == null) {
	        throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
	    }
	    
	    this.id = id;
	    this.transactionFactory = transactionFactory;
	    this.dataSource = dataSource;
		    
	}
	
	/**
	 * 静态内部类builder，用了建造者设计模式
	 * 用法：Environment env = new Environment.Builder(id).transactionFactory(xx).dataSource(xx).build();
	 * 建造者设计模式：构建者模式一般用于构建复杂对象时，将复杂对象分割成许多小对象进行分别构建，然后整合在一起形成一个大对象，这样做能很好的规范对象构建的细节过程
	 * 这里也是一样的目的，虽然说Environment类的字段较少，但在MyBatis中大量使用构建者模式的基础上，在此处使用构建者模式也无可厚非，而且通过内部类的方式构建，
	 * 这个Environment对象的创建会在内部类构建方法build()被显式调用时才会在内存中创建，实现了懒加载。这又有点单例模式的意思在内，
	 * 虽然Mybatis中可创建多个Environment环境，但是在正式运行时，只会存在一个环境，确实是使用内部类实现了懒加载的单例模式。
	 * 
	 * @author ethan
	 *
	 */
	public static class Builder {
		private String id;
		private TransactionFactory transactionFactory;
		private DataSource dataSource;
		
		//返回this，用到链式编程，下同
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder transactionFactory(TransactionFactory transactionFactory) {
			this.transactionFactory = transactionFactory;
			return this;
		}
		
		public Builder dataSource(DataSource dataSource) {
			this.dataSource = dataSource;
			return this;
		}
		
		public String id() {
			return this.id;
		}
		
		public Environment build() {
			return new Environment(this.id, this.transactionFactory, this.dataSource);
		}
	}
	
	public String getId() {
		return this.id;
	}
	
	public TransactionFactory getTransactionFactory() {
		return this.transactionFactory;
	}
	
	public DataSource getDataSource() {
		return this.dataSource;
	}
}
