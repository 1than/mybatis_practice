package com.test.mybatis.session;

import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

import com.test.mybatis.io.Resources;
import com.test.mybatis.mapper.Role;
import com.test.mybatis.mapper.RoleMapper;

public class SessionTest {

	@Test
	public void test1() throws IOException {
		Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionFactory.openSession();
			
			RoleMapper roleMapper = sqlSession.getMapper(RoleMapper.class);
			System.out.println("代理对象");
			System.out.println(roleMapper.getClass().getName());
			Role role = roleMapper.getRoleById(1);
			System.out.println("结果。。。");
			System.out.println(role);
			sqlSession.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sqlSession != null) {
				sqlSession.close();
			}
		}
	}

}
