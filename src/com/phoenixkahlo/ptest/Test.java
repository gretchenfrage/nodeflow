package com.phoenixkahlo.ptest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation for public static voids that are tests. Testing.test will
 * execute any method with this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

	String name() default "$unnamed";
	
}
