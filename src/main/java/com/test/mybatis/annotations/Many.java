package com.test.mybatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.test.mybatis.mapping.FetchType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Many {

	String select() default "";

	FetchType fetchType() default FetchType.DEFAULT;

}
