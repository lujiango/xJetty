package org.github.x.jetty.core;



public @interface Entry {
	public abstract String[] path();
	public abstract Type[] type();
	public abstract String body();
	public abstract String dateFormat();
	public abstract String charset();
	public abstract int startup();
	public abstract int maxThreadNum();
	public abstract int timeLimit();
	
	public static  enum Type {
		GET, POST, PUT, DELETE, OPTIONS, HEAD, INTERNAL, PATCH, PROPFIND, REPORT;
	}
}
