package com.test.mybatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import javax.sql.DataSource;

import com.test.mybatis.builder.BaseBuilder;
import com.test.mybatis.builder.BuilderException;
import com.test.mybatis.datasource.DataSourceFactory;
import com.test.mybatis.executor.ErrorContext;
import com.test.mybatis.executor.loader.ProxyFactory;
import com.test.mybatis.io.Resources;
import com.test.mybatis.io.VFS;
import com.test.mybatis.logging.Log;
import com.test.mybatis.mapping.DatabaseIdProvider;
import com.test.mybatis.mapping.Environment;
import com.test.mybatis.parsing.XNode;
import com.test.mybatis.parsing.XPathParser;
import com.test.mybatis.plugin.Interceptor;
import com.test.mybatis.reflection.DefaultReflectorFactory;
import com.test.mybatis.reflection.MetaClass;
import com.test.mybatis.reflection.ObjectFactory;
import com.test.mybatis.reflection.ReflectorFactory;
import com.test.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.test.mybatis.session.AutoMappingBehavior;
import com.test.mybatis.session.AutoMappingUnknownColumnBehavior;
import com.test.mybatis.session.Configuration;
import com.test.mybatis.session.ExecutorType;
import com.test.mybatis.session.LocalCacheScope;
import com.test.mybatis.transaction.TransactionFactory;
import com.test.mybatis.type.JdbcType;

/**
 * XMLConfigBuilder是BaseBuilder的子类，主要负责解析XmlBuilder配置文件
 * 
 * @author ethan
 *
 */
public class XMLConfigBuilder extends BaseBuilder {

	//是否以解析过
	private boolean parsed;
	
	//用于解析mybatis-config.xml配置文件的具体解析对象
	private final XPathParser parser;
	
	//解析mybatis-config.xml 中的<environment>对象，默认读取default属性
	private String environment;
	
	//负责创建和缓存Reflector对象
	private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

	public XMLConfigBuilder(Reader reader) {
		this(reader, null, null);
	}

	public XMLConfigBuilder(Reader reader, String environment) {
		this(reader, environment, null);
	}

	public XMLConfigBuilder(Reader reader, String environment, Properties props) {
		this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
	}

	public XMLConfigBuilder(InputStream inputStream) {
		this(inputStream, null, null);
	}

	public XMLConfigBuilder(InputStream inputStream, String environment) {
		this(inputStream, environment, null);
	}

	public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
		this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
	}

	private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
		super(new Configuration());
		System.out.println("初始化 XMLConfigBuilder...");
		ErrorContext.instance().resource("SQL Mapper Configuration");
		this.configuration.setVariables(props);
		this.parsed = false;
		this.environment = environment;
		this.parser = parser;
	}

	public Configuration parse() {
		if (parsed) {
			throw new BuilderException("Each XMLConfigBuilder can only be used once.");
		}
		parsed = true;
		parseConfiguration(parser.evalNode("/configuration"));
		return configuration;
	}

	private void parseConfiguration(XNode root) {
		try {
			// issue #117 read properties first
			
			//解析配置文件中的 <properties>节点
			propertiesElement(root.evalNode("properties"));
			
			//解析配置文件中的<settings>节点，该节点下的配置是全局性的，会改变mybatis的运行时行为
			Properties settings = settingsAsProperties(root.evalNode("settings"));
			loadCustomVfs(settings);
			loadCustomLogImpl(settings);
			
			//解析＜typeAlias>标签
			typeAliasesElement(root.evalNode("typeAliases"));
			
			//解析<plugins>节点用户可以通过自定义插件在SQL语句执行过程中的某一点进行拦截
			pluginElement(root.evalNode("plugins"));
			
			//解析ObjectFactory节点
			objectFactoryElement(root.evalNode("objectFactory"));
			objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
			reflectorFactoryElement(root.evalNode("reflectorFactory"));
			settingsElement(settings);
			
			// read it after objectFactory and objectWrapperFactory issue #631
			//解析encironments节点，如开发，测试，生产环境，不同的环境针对不同的配置，每个SqlSessionFactory只能选择其中的一个
			environmentsElement(root.evalNode("environments"));
			
			/**
			 * 解析databaseIdProvider
			 * 
			 * 通过databaseIdProvider定义所有支持的数据库的datasourceId，然后在映射配置文件中定义sql语句节点时，通过datasourceId指定
			 * 该sql语句应用的数据库
			 * 
			 * 在mybatis初始化时，会根据前面确定的DataSource确定当前使用的数据库，然后在解析sql语句的时候会加载不带datasourceId
			 * 和带有匹配当前数据库datasourceId属性的所有sql语句，如果同时找到带有datasourceId和不带有datasourceId的相同语句，
			 * 则后者会被舍弃，使用前者
			 */
			databaseIdProviderElement(root.evalNode("databaseIdProvider"));
			
			//解析typeHandlers节点
			typeHandlerElement(root.evalNode("typeHandlers"));
			
			/**
			 * 
			 * 解析mappers节点
			 * 
			 * 在myabtis初始化时，除了加载mybatis-config.xml配置文件，还会加载全部的映射配置文件。mybatis-config.xml配置文件中的<mappers>
			 * 会告诉mybatis去哪些位置找映射配置文件以及使用了配置注解标示的接口
			 *
			 */
			mapperElement(root.evalNode("mappers"));
		} catch (Exception e) {
			throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
		}
	}

	private Properties settingsAsProperties(XNode context) {
		
		//解析settiings下的name和value属性，并返回properties对象
		if (context == null) {
			//若没有配置settings，则返回空对象properties
			return new Properties();
		}
		
		//解析具体的name和value并转化为properties对象
		Properties props = context.getChildrenAsProperties();
		
		// Check that all settings are known to the configuration class
		MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
		for (Object key : props.keySet()) {
			if (!metaConfig.hasSetter(String.valueOf(key))) {
				throw new BuilderException(
						"The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
			}
		}
		return props;
	}

	private void loadCustomVfs(Properties props) throws ClassNotFoundException {
		String value = props.getProperty("vfsImpl");
		if (value != null) {
			String[] clazzes = value.split(",");
			for (String clazz : clazzes) {
				if (!clazz.isEmpty()) {
					@SuppressWarnings("unchecked")
					Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
					configuration.setVfsImpl(vfsImpl);
				}
			}
		}
	}

	private void loadCustomLogImpl(Properties props) {
		Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
		configuration.setLogImpl(logImpl);
	}

	private void typeAliasesElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String typeAliasPackage = child.getStringAttribute("name");
					//解析出来之后直接注册到configuration的TypeAliasRegistry中
					configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
				} else {
					String alias = child.getStringAttribute("alias");
					String type = child.getStringAttribute("type");
					try {
						Class<?> clazz = Resources.classForName(type);
						if (alias == null) {
							typeAliasRegistry.registerAlias(clazz);
						} else {
							typeAliasRegistry.registerAlias(alias, clazz);
						}
					} catch (ClassNotFoundException e) {
						throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
					}
				}
			}
		}
	}

	private void pluginElement(XNode parent) throws Exception {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				String interceptor = child.getStringAttribute("interceptor");
				Properties properties = child.getChildrenAsProperties();
				Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor()
						.newInstance();
				interceptorInstance.setProperties(properties);
				configuration.addInterceptor(interceptorInstance);
			}
		}
	}

	private void objectFactoryElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties properties = context.getChildrenAsProperties();
			ObjectFactory factory = (ObjectFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(properties);
			configuration.setObjectFactory(factory);
		}
	}

	private void objectWrapperFactoryElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).getDeclaredConstructor()
					.newInstance();
			configuration.setObjectWrapperFactory(factory);
		}
	}

	private void reflectorFactoryElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			ReflectorFactory factory = (ReflectorFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			configuration.setReflectorFactory(factory);
		}
	}

	private void propertiesElement(XNode context) throws Exception {
		if (context != null) {
			Properties defaults = context.getChildrenAsProperties();
			String resource = context.getStringAttribute("resource");
			String url = context.getStringAttribute("url");
			
			//<properties>标签中，不能同时存在resource和url
			if (resource != null && url != null) {
				throw new BuilderException(
						"The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
			}
			
			//先解析resource 若没有则解析url
			if (resource != null) {
				defaults.putAll(Resources.getResourceAsProperties(resource));
			} else if (url != null) {
				defaults.putAll(Resources.getUrlAsProperties(url));
			}
			
			//若configuration中已经有properties属性，则更新，否则直接set
			Properties vars = configuration.getVariables();
			if (vars != null) {
				defaults.putAll(vars);
			}
			parser.setVariables(defaults);
			configuration.setVariables(defaults);
		}
	}

	private void settingsElement(Properties props) {
		configuration.setAutoMappingBehavior(
				AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
		configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior
				.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
		configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
		configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
		configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
		configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
		configuration
				.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
		configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
		configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
		configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
		configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
		configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
		configuration.setDefaultResultSetType(resolveResultSetType(props.getProperty("defaultResultSetType")));
		configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
		configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
		configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
		configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
		configuration.setLazyLoadTriggerMethods(
				stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
		configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
		configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
		configuration.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
		configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
		configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
		configuration
				.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
		configuration.setLogPrefix(props.getProperty("logPrefix"));
		configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
	}

	private void environmentsElement(XNode context) throws Exception {
		if (context != null) {
			if (environment == null) {
				environment = context.getStringAttribute("default");
			}
			for (XNode child : context.getChildren()) {
				String id = child.getStringAttribute("id");
				if (isSpecifiedEnvironment(id)) {
					TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
					DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
					DataSource dataSource = dsFactory.getDataSource();
					Environment.Builder environmentBuilder = new Environment.Builder(id).transactionFactory(txFactory)
							.dataSource(dataSource);
					configuration.setEnvironment(environmentBuilder.build());
				}
			}
		}
	}

	private void databaseIdProviderElement(XNode context) throws Exception {
		DatabaseIdProvider databaseIdProvider = null;
		if (context != null) {
			String type = context.getStringAttribute("type");
			// awful patch to keep backward compatibility
			if ("VENDOR".equals(type)) {
				type = "DB_VENDOR";
			}
			Properties properties = context.getChildrenAsProperties();
			databaseIdProvider = (DatabaseIdProvider) resolveClass(type).getDeclaredConstructor().newInstance();
			databaseIdProvider.setProperties(properties);
		}
		Environment environment = configuration.getEnvironment();
		if (environment != null && databaseIdProvider != null) {
			String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
			configuration.setDatabaseId(databaseId);
		}
	}

	private TransactionFactory transactionManagerElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties props = context.getChildrenAsProperties();
			TransactionFactory factory = (TransactionFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(props);
			return factory;
		}
		throw new BuilderException("Environment declaration requires a TransactionFactory.");
	}

	private DataSourceFactory dataSourceElement(XNode context) throws Exception {
		if (context != null) {
			String type = context.getStringAttribute("type");
			Properties props = context.getChildrenAsProperties();
			DataSourceFactory factory = (DataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
			factory.setProperties(props);
			return factory;
		}
		throw new BuilderException("Environment declaration requires a DataSourceFactory.");
	}

	private void typeHandlerElement(XNode parent) {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String typeHandlerPackage = child.getStringAttribute("name");
					typeHandlerRegistry.register(typeHandlerPackage);
				} else {
					String javaTypeName = child.getStringAttribute("javaType");
					String jdbcTypeName = child.getStringAttribute("jdbcType");
					String handlerTypeName = child.getStringAttribute("handler");
					Class<?> javaTypeClass = resolveClass(javaTypeName);
					JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
					Class<?> typeHandlerClass = resolveClass(handlerTypeName);
					if (javaTypeClass != null) {
						if (jdbcType == null) {
							typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
						} else {
							typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
						}
					} else {
						typeHandlerRegistry.register(typeHandlerClass);
					}
				}
			}
		}
	}

	private void mapperElement(XNode parent) throws Exception {
		if (parent != null) {
			for (XNode child : parent.getChildren()) {
				if ("package".equals(child.getName())) {
					String mapperPackage = child.getStringAttribute("name");
					
					//注册到configuration的mapperRegistry中，下同
					configuration.addMappers(mapperPackage);
				} else {
					//抓取mapper节点的resource，url，class属性，这三个属性互斥，不能同时存在
					String resource = child.getStringAttribute("resource");
					String url = child.getStringAttribute("url");
					String mapperClass = child.getStringAttribute("class");
					//如果mapper节点制定了resource或者是url属性，则创建XMLMapperBuilder对象
					//并通过该对象解析resource或是url属性指定的Mapper配置文件
					if (resource != null && url == null && mapperClass == null) {
						//resource
						ErrorContext.instance().resource(resource);
						InputStream inputStream = Resources.getResourceAsStream(resource);
						XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource,
								configuration.getSqlFragments());
						mapperParser.parse();
					} else if (resource == null && url != null && mapperClass == null) {
						//url
						ErrorContext.instance().resource(url);
						InputStream inputStream = Resources.getUrlAsStream(url);
						XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url,
								configuration.getSqlFragments());
						mapperParser.parse();
					} else if (resource == null && url == null && mapperClass != null) {
						//class
						Class<?> mapperInterface = Resources.classForName(mapperClass);
						configuration.addMapper(mapperInterface);
					} else {
						throw new BuilderException(
								"A mapper element may only specify a url, resource or class, but not more than one.");
					}
				}
			}
		}
	}

	private boolean isSpecifiedEnvironment(String id) {
		if (environment == null) {
			throw new BuilderException("No environment specified.");
		} else if (id == null) {
			throw new BuilderException("Environment requires an id attribute.");
		} else if (environment.equals(id)) {
			return true;
		}
		return false;
	}
}
