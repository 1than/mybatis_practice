package com.test.mybatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.test.mybatis.mapping.StatementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKey {

	String[] statement();

	String keyProperty();

	String keyColumn() default "";

	boolean before();

	Class<?> resultType();

	StatementType statementType() default StatementType.PREPARED;
}
