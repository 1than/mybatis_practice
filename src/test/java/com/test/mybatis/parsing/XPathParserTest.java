package com.test.mybatis.parsing;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.junit.Test;

import com.test.mybatis.io.Resources;

public class XPathParserTest {
	
	private String resource = "com/test/mybatis/parsing/nodelet_test.xml";
	
	@Test
	public void test1() {
		
		try {
			InputStream in = Resources.getResourceAsStream("com/test/mybatis/parsing/nodelet_test.xml");
			XPathParser parser = new XPathParser(in, false, null, null);
			assertNotNull(parser);
			testEvalMethod(parser);
			System.out.println("first_name = " + parser.evalString("/employee/first_name"));
			System.out.println("year = " + parser.evalInteger("/employee/birth_date/year"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void test2() {
		InputStream in;
		try {
			in = Resources.getResourceAsStream("com/test/mybatis/parsing/nodelet_test.xml");
			XPathParser parser = new XPathParser(in, false, null);
			assertNotNull(parser);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test3() {
		try {
			InputStream in = Resources.getResourceAsStream(resource);
			XPathParser parser = new XPathParser(in, false);
			assertNotNull(parser);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test4() {
		try {
			InputStream in = Resources.getResourceAsStream(resource);
			XPathParser parser = new XPathParser(in);
			assertNotNull(parser);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test5() {
		try {
			Reader reader = Resources.getResourceAsReader(resource);
			XPathParser parser = new XPathParser(reader,false, null, null);
			assertNotNull(parser);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test6() {
		try {
			Reader reader = Resources.getResourceAsReader(resource);
			XPathParser parser = new XPathParser(reader, false, null);
			assertNotNull(parser);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test7() {
		try {
			Reader reader = Resources.getResourceAsReader(resource);
			XPathParser parser = new XPathParser(reader, false);
			assertNotNull(parser);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test8() {
		try {
			Reader reader = Resources.getResourceAsReader(resource);
			XPathParser parser = new XPathParser(reader);
			assertNotNull(parser);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void testEvalMethod(XPathParser parser) {
	    assertEquals((Long) 1970L, parser.evalLong("/employee/birth_date/year"));
	    assertEquals((short) 6, (short) parser.evalShort("/employee/birth_date/month"));
	    assertEquals((Integer) 15, parser.evalInteger("/employee/birth_date/day"));
	    assertEquals((Float) 5.8f, parser.evalFloat("/employee/height"));
	    assertEquals((Double) 5.8d, parser.evalDouble("/employee/height"));
	    assertEquals("${id_var}", parser.evalString("/employee/@id"));
	    assertEquals(Boolean.TRUE, parser.evalBoolean("/employee/active"));
	    assertEquals("<id>${id_var}</id>", parser.evalNode("/employee/@id").toString().trim());
	    assertEquals(7, parser.evalNodes("/employee/*").size());
	    XNode node = parser.evalNode("/employee/height");
	    assertEquals("employee/height", node.getPath());
	    assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
	  }

}
