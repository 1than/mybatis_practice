package com.test.mybatis.builder;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.test.mybatis.builder.xml.XMLConfigBuilder;
import com.test.mybatis.io.Resources;
import com.test.mybatis.session.Configuration;


public class XmlConfigBuilderTest {
	
	/**
	 * 
	 * 测试XMLConfigBuilder
	 * 
	 */
	@Test
	public void test1() {
		String resource = "com/test/mybatis/builder/MinimalMapperConfig.xml";
		try {
			InputStream in = Resources.getResourceAsStream(resource);
			XMLConfigBuilder builder = new XMLConfigBuilder(in);
			Configuration config = builder.parse();
			System.out.println(config.getAutoMappingBehavior());
			System.out.println(config.getAutoMappingUnknownColumnBehavior());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
