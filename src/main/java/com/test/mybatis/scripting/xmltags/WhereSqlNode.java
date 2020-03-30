package com.test.mybatis.scripting.xmltags;

import java.util.Arrays;
import java.util.List;

import com.test.mybatis.session.Configuration;


public class WhereSqlNode extends TrimSqlNode {

	private static List<String> prefixList = Arrays.asList("AND ","OR ","AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

	  public WhereSqlNode(Configuration configuration, SqlNode contents) {
	    super(configuration, contents, "WHERE", prefixList, null, null);
	  }
}
