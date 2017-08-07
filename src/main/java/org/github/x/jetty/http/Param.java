package org.github.x.jetty.http;

public @interface Param {
	public abstract String name();
	public abstract String defaultValue();
	
	public abstract Type type();
	
	public abstract String format();
	
	public static enum Type {
		String, Number, Boolean, Object, Array, Auto, Ignore;
	}
}
