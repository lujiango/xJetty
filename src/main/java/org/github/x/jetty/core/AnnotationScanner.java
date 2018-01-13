package org.github.x.jetty.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import org.apache.log4j.Logger;
import org.github.x.jetty.http.Param;
import org.github.x.jetty.utils.ReflectUtils;
import org.github.x.jetty.validate.Register;
import org.github.x.jetty.validate.ZKConf;
import org.github.x.jetty.validate.annotation.Constraint;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.FieldAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationObjectDiscoveryListener;

public final class AnnotationScanner {
	private static final Logger LOG = Logger.getLogger(AnnotationScanner.class);
	
	static Map<String, EntryInfo> url2method = new HashMap<String, EntryInfo>();
	static Map<String, Set<Class<?>>> server2class = new HashMap<String, Set<Class<?>>>();
	private static ClassPool classPool = ClassPool.getDefault();
	static void scanSupportedAnnotations() {
		LOG.info("start scan annotations...");
		long start = System.currentTimeMillis();
		Discoverer discover = new ClasspathDiscoverer();
		discover.addAnnotationListener(new MyClassAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyFieldAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyMethodAnnotationObjectDiscoveryListener());
		discover.discover(true, true, true, true, true, true);
		LOG.info("finished scan annotations in " + (System.currentTimeMillis() - start) + "ms");
	}

	static class MyClassAnnotationObjectDiscoveryListener implements ClassAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Register.class.getName() };
		}

		public void discovered(ClassFile clazz, Annotation annotation) {
			
		}

	}

	static class MyMethodAnnotationObjectDiscoveryListener implements MethodAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Entry.class.getName() };
		}
		private void addMapping(String path, EntryInfo info) {
			String tmp = path.split(" ")[1];
			if ((tmp.length() > 1) && (tmp.charAt(tmp.length() - 1) == '/')) {
				path = path.substring(0, path.length() -1);
			}
			Map<String, Integer> map = new HashMap<String, Integer>();
			int i = 0;		
			for (String  s :  path.split("/")) {
						if (s.matches("\\{[a-zA-Z0-9\\-\\_]+\\}")) {
							map.put(s.substring(1, s.length() -1), Integer.valueOf(i));
						}
						i++;
					}
			info.groupMap = map;
			path = path.replaceAll("\\{[a-zA-Z0-9\\-\\_]+\\}", "*");
			url2method.put(path, info);
		}

		public void discovered(ClassFile classFile, MethodInfo methodInfo, Annotation annotation) {
			Method m = null;
			String[] paramNames = null;
			Class<?> c;
			try {
				c = Class.forName(classFile.getName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
			Class[] paramTypes = ReflectUtils.paramTypes(methodInfo.getDescriptor());
			try {
				m = c.getDeclaredMethod(methodInfo.getName(), paramTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			if (m != null) {
				CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
				LocalVariableAttribute attr = (LocalVariableAttribute)codeAttribute.getAttribute("LocalVariableTable");
				CtClass[] paramCt = new CtClass[paramTypes.length];
				for (int i = 0; i < paramTypes.length; i++) {
					paramCt[i] = classPool.getCtClass(paramTypes[i].getName());
				}
				CtMethod cm;
				try {
					cm = classPool.getCtClass(classFile.getName()).getDeclaredMethod(methodInfo.getName(), paramCt);
				} catch (NotFoundException e) {
					e.printStackTrace();
					return;
				}
				
				int paramNum;
				try {
					paramNum = cm.getParameterTypes().length;
					paramNames = new String[paramNum];
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
				if (attr != null) {
					String[] attrSymbolNames = new String[attr.tableLength()];
					for (int i = 0; i < attr.tableLength(); i++) {
						attrSymbolNames[attr.index(i)] = attr.variableName(i);
					}
					
					for( int i = 0; i < paramNum; i++) {
						paramNames[i] = attrSymbolNames[i + pos];
					}
				}
				
				Entry entry = m.getAnnotation(Entry.class);
				Entry.Type[] mm = entry.type();
				
				String[] uu = entry.path();
				
				if (uu == null || uu.length == 0) {
					uu = new String[]{new StringBuilder().append('#').append(c.getName()).append('/').append(m.getName()).toString()};
					mm = new Entry.Type[] {Entry.Type.INTERNAL};
				}
				
				for (int i = 0; i < uu.length; i++) {
					for (int j = 0; j < mm.length; j++) {
						String path = uu[i];
						EntryInfo info = new EntryInfo();
						info.url = uu[i];
						info.startup = entry.startup();
						info.type = mm[j];
						info.clazz = m.getDeclaringClass();
						info.method = m;
						
						java.lang.annotation.Annotation[][] aas = m.getParameterAnnotations();
						Param[] as = new Param[aas.length];
						Map<Integer, Set<java.lang.annotation.Annotation>> paramVM = new ConcurrentHashMap<Integer, Set<java.lang.annotation.Annotation>>();
						for (int k = 0; k < as.length; k++) {
							Set<java.lang.annotation.Annotation> validateAnnotationSet = new LinkedHashSet<java.lang.annotation.Annotation>();
							for (int kk = 0; kk < aas[k].length; kk++) {
								if (aas[k][kk].annotationType().isAssignableFrom(Param.class)) {
									as[k] = ((Param)aas[k][kk]);
								}
								
								if (aas[k][kk].annotationType().getAnnotation(Constraint.class) != null) {
									validateAnnotationSet.add(aas[k][kk]);
								}
							}
							if (!validateAnnotationSet.isEmpty()) {
								paramVM.put(Integer.valueOf(k), validateAnnotationSet);
							}
						}
						info.paramAnns = as;
						info.paramValidateMap = paramVM;
						info.paramNames = paramNames;
						info.bodyParamName = entry.body();
						info.timeLimit = entry.timeLimit();
						
						if ((!info.bodyParamName.isEmpty()) && (info.type == Entry.Type.GET)) {
							info.type = Entry.Type.POST;
						}
						if (info.url.indexOf('#') != -1) {
							info.type = Entry.Type.INTERNAL;
						}
						
						info.dateFormat = entry.dateFormat();
						info.charset = entry.charset();
						for (int p = 0; p < paramNames.length; p++) {
							if (paramNames[p].equals(info.bodyParamName)) {
								info.bodyParamType = info.paramTypes[p];
								info.bodyParamIndex = p;
							}
						}
						
						if ((!info.bodyParamName.isEmpty()) && (info.bodyParamType == null)) {
							throw new IllegalArgumentException(new StringBuilder().append("body attribute ").append(info.bodyParamName).append(" does not match any parameter of method ").append(ReflectUtils.desc(m, info.paramNames)).toString());
						}
						if (entry.maxThreadNum() > 0) {
							info.maxThreadNum = entry.maxThreadNum();
						} else {
							info.maxThreadNum = 100;
						}
						info.threadCount = new AtomicInteger(0);
						path = new StringBuilder().append(info.type.name()).append(' ').append(path).toString();
						addMapping(path, info);
						LOG.info("Map " + path + " ==> " + ReflectUtils.desc(m, paramNames));
					}
				}
					
			}
			
			
		}

	}

	static class MyFieldAnnotationObjectDiscoveryListener implements FieldAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { ZKConf.class.getName() };
		}

		public void discovered(ClassFile clazz, FieldInfo field, Annotation annotation) {

		}

	}

}
