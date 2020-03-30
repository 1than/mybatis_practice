package com.test.mybatis.scripting.xmltags;


import com.test.mybatis.io.Resources;

import ognl.DefaultClassResolver;

public class OgnlClassResolver extends DefaultClassResolver {

	@Override
	  protected Class toClassForName(String className) throws ClassNotFoundException {
	    return Resources.classForName(className);
	  }
}
