package org.github.x.jetty.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.github.x.jetty.http.Param;

public class EntryInfo implements Cloneable {
	Class<?> clazz;
	Method method;
	int startup;
	String url;
	Entry.Type type;
	String dateFormat;
	String charset;
	public Param[] paramAnns;
	String[] paramNames;
	Class<?>[] paramTypes;
	String bodyParamName;
	int bodyParamIndex;
	Class<?> bodyParamType;
	Map<String, Integer> groupMap;
	int maxThreadNum;
	AtomicInteger threadCount;
	int timeLimit;
	Map<Integer, Set<Annotation>> paramValidateMap;
	@Override
	protected Object clone() throws CloneNotSupportedException {
		EntryInfo info  = (EntryInfo)super.clone();
		info.paramAnns = (Param[])Arrays.copyOf(this.paramAnns, this.paramAnns.length);
		
		return super.clone();
	}
	
}
