package com.test.mybatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.test.mybatis.type.JdbcType;
import com.test.mybatis.type.TypeHandler;
import com.test.mybatis.type.UnknownTypeHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TypeDiscriminator {

	String column();

	Class<?> javaType() default void.class;

	JdbcType jdbcType() default JdbcType.UNDEFINED;

	Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;

	Case[] cases();
}
