package org.github.x.jetty.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry {
	public abstract String[] path() default {};

	public abstract Type[] type() default { Type.GET };

	public abstract String body() default "";

	public abstract String dateFormat() default "";

	public abstract String charset() default "";

	public abstract int startup() default Integer.MAX_VALUE;

	public abstract int maxThreadNum() default 100;

	public abstract int timeLimit() default 60000;

	public static enum Type {
		GET, POST, PUT, DELETE, OPTIONS, HEAD, INTERNAL, PATCH, PROPFIND, REPORT;
	}
}
