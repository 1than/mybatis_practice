package com.test.mybatis.io;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class ClassLoaderWrapperTest {

	private final String RESOURCE_NOT_FOUND = "some_resource_that_does_not_exist.properties";
	private final String CLASS_NOT_FOUND = "some.random.class.that.does.not.Exist";
	private final String CLASS_FOUND = "java.lang.Object";
	private final String JPETSTORE_PROPERTIES = "com/test/mybatis/io/jpetstore-hsqldb.properties";
	private final String MYBATIS_CONFIG = "mybatis-config.xml";
	
	private ClassLoader classLoader = getClass().getClassLoader();

	@Test
	public void test1() {

		ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();
		assertNull(classLoaderWrapper.defaultClassLoader);
		assertNotNull(classLoaderWrapper.systemClassLoader);
	}
	
	@Test
	public void test2() {
		URL url = new ClassLoaderWrapper().getResourceAsURL(RESOURCE_NOT_FOUND);
		assertNull(url);
	}
	
	@Test
	public void test3() {
		URL url = new ClassLoaderWrapper().getResourceAsURL(JPETSTORE_PROPERTIES);
		assertNotNull(url);
	} 
	
	@Test
	public void test4() {
		URL url = new ClassLoaderWrapper().getResourceAsURL(MYBATIS_CONFIG);
		assertNotNull(url);
	}
	
	@Test
	public void test5() {
		URL url = new ClassLoaderWrapper().getResourceAsURL("mappers/test3.txt");
		assertNotNull(url);
	}
	
	@Test
	public void test6() {
		InputStream in = new ClassLoaderWrapper().getResourceAsStream(MYBATIS_CONFIG, classLoader);
		assertNotNull(in);
	}
	
	@Test
	public void test7() throws ClassNotFoundException {
		Class<String> clazz = (Class<String>) new ClassLoaderWrapper().classForName("java.lang.String");
		assertNotNull(clazz);
		System.out.println(clazz.getName());
		System.out.println(clazz.getSimpleName());
		System.out.println(clazz.getTypeName());
		System.out.println(clazz.getSuperclass());
	}
	

}
