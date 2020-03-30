package com.test.mybatis.io;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

public class ResourcesTest {
	
	private ClassLoader loader = getClass().getClassLoader();
	
	private final String MYBATIS_CONFIG = "mybatis-config.xml";
	
	@Test
	public void test1() throws IOException {
		URL url = Resources.getResourceURL("com/test/mybatis/io/jpetstore-hsqldb.properties");
		assertNotNull(url);
	}
	
	@Test
	public void test2() throws IOException {
		URL url = Resources.getResourceURL(MYBATIS_CONFIG);
		assertNotNull(url);
	}
	
	@Test
	public void test3() throws IOException {
		Properties properties = Resources.getResourceAsProperties(loader, "com/test/mybatis/io/jpetstore-hsqldb.properties");
		assertNotNull(properties);
		assertEquals("sa", properties.get("username"));
		assertEquals("sa", properties.get("password"));
		assertEquals("org.hsqldb.jdbcDriver", properties.get("driver"));
		assertEquals("jdbc:hsqldb:.", properties.get("url"));
	}

}
