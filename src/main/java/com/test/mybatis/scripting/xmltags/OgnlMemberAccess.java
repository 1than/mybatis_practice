package com.test.mybatis.scripting.xmltags;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;

import com.test.mybatis.reflection.Reflector;

import ognl.MemberAccess;


class OgnlMemberAccess implements MemberAccess {

	private final boolean canControlMemberAccessible;

	OgnlMemberAccess() {
		this.canControlMemberAccessible = Reflector.canControlMemberAccessible();
	}

	@Override
	public Object setup(Map context, Object target, Member member, String propertyName) {
		Object result = null;
		if (isAccessible(context, target, member, propertyName)) {
			AccessibleObject accessible = (AccessibleObject) member;
			if (!accessible.isAccessible()) {
				result = Boolean.FALSE;
				accessible.setAccessible(true);
			}
		}
		return result;
	}

	@Override
	public void restore(Map context, Object target, Member member, String propertyName, Object state) {
		// Flipping accessible flag is not thread safe. See #1648
	}

	@Override
	public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
		return canControlMemberAccessible;
	}
}
