package org.github.x.jetty.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.github.x.jetty.core.Entry.Type;
import org.github.x.jetty.utils.ReflectUtils;
import org.github.x.jetty.validate.Register;
import org.github.x.jetty.validate.ZKConf;
import org.github.x.jetty.validate.annotation.Constraint;
import org.github.x.jetty.validate.annotation.Param;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.FieldAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationObjectDiscoveryListener;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

public final class AnnotationScanner {
	private static final Logger LOG = Logger.getLogger(AnnotationScanner.class);

	static Map<String, EntryInfo> url2method = new HashMap<String, EntryInfo>();
	static Map<String, Set<Class<?>>> server2class = new HashMap<String, Set<Class<?>>>();
	private static ClassPool classPool = ClassPool.getDefault();

	static void scanSupportedAnnotations() {
		LOG.info("Starting scan annotations...");
		long start = System.currentTimeMillis();
		Discoverer discover = new ClasspathDiscoverer();
		discover.addAnnotationListener(new MyClassAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyFieldAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyMethodAnnotationObjectDiscoveryListener());
		discover.discover(true, true, true, true, true, true);
		LOG.info("Finished scan annotations in " + (System.currentTimeMillis() - start) + "ms");
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
				path = path.substring(0, path.length() - 1);
			}
			Map<String, Integer> map = new HashMap<String, Integer>();
			int i = 0;
			for (String s : path.split("/")) {
				if (s.matches("\\{[a-zA-Z0-9\\-\\_]+\\}")) {
					map.put(s.substring(1, s.length() - 1), Integer.valueOf(i));
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
            try {
                Class<?> c = Class.forName(classFile.getName());
                Class<?>[] paramTypes = ReflectUtils.paramTypes(methodInfo.getDescriptor());
                m = c.getDeclaredMethod(methodInfo.getName(), paramTypes);
                if (m != null) {
                    // 用javassist获得方法的参数变量名
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                            .getAttribute(LocalVariableAttribute.tag);
                    CtClass[] paramCt = new CtClass[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        paramCt[i] = classPool.getCtClass(paramTypes[i].getName());
                    }
                    CtMethod cm = classPool.getCtClass(classFile.getName()).getDeclaredMethod(methodInfo.getName(),
                            paramCt);
                    int paramNum = cm.getParameterTypes().length;
                    paramNames = new String[paramNum];
                    int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;

                    if (attr != null) {
                        // 此处对字节序符号做排序处理，已避免ecj和javac编译出来的差距
                        String[] attrSymbolNames = new String[attr.tableLength()];
                        for (int i = 0; i < attr.tableLength(); i++) {
                            try {
                                attrSymbolNames[attr.index(i)] = attr.variableName(i);
                            } catch (Exception ex) {
                                // 符号解析出错，解析到无用的字符名忽略
                                continue;
                            }
                        }

                        for (int i = 0; i < paramNum; i++) {
                            paramNames[i] = attrSymbolNames[i + pos];
                        }
                    }

                    Entry entry = m.getAnnotation(Entry.class);
                    Entry.Type mm[] = entry.type();

                    String[] uu = entry.path();
                    if (uu == null || uu.length == 0) {
                        uu = new String[] {"#" + c.getName() + "/" + m.getName()};
                        mm = new Entry.Type[] {Entry.Type.INTERNAL};
                    }

                    // path允许配多个，依次处理
                    for (int i = 0; i < uu.length; i++) {
                        // type也允许配多个，依次处理
                        for (int j = 0; j < mm.length; j++) {
                            String path = uu[i];
                            EntryInfo info = new EntryInfo();
                            info.url = uu[i];
                            info.startup = entry.startup();
                            info.type = mm[j];
                            info.clazz = m.getDeclaringClass();
                            info.method = m;

                            // 如果参数带@Param注释则将其填入
                            java.lang.annotation.Annotation[][] aas = m.getParameterAnnotations();
                            Param[] as = new Param[aas.length];

                            // 纪录参数校验器
                            Map<Integer, Set<java.lang.annotation.Annotation>> paramVM = new ConcurrentHashMap<Integer, Set<java.lang.annotation.Annotation>>();
                            for (int k = 0; k < as.length; k++) {
                                Set<java.lang.annotation.Annotation> validateAnnotationSet = new LinkedHashSet<java.lang.annotation.Annotation>();
                                for (int kk = 0; kk < aas[k].length; kk++) {
                                    if (aas[k][kk].annotationType().isAssignableFrom(Param.class)) {
                                        as[k] = (Param) aas[k][kk];
                                    }

                                    if (aas[k][kk].annotationType().getAnnotation(Constraint.class) != null) {
                                        validateAnnotationSet.add(aas[k][kk]);
                                    }
                                }
                                if (!validateAnnotationSet.isEmpty()) {
                                    paramVM.put(k, validateAnnotationSet);
                                }
                            }
                            info.paramAnns = as;

                            info.paramValidateMap = paramVM;

                            info.paramNames = paramNames;
                            info.paramTypes = m.getParameterTypes();
                            info.bodyParamName = entry.body();
                            info.timeLimit = entry.timeLimit();

                            // 如果有body参数，则不应该是GET请求
                            if (!info.bodyParamName.isEmpty() && info.type == Type.GET) {
                                info.type = Type.POST;
                            }

                            // 对序列化失败处理
                            if (info.url.indexOf("#") != -1) {
                                info.type = Type.INTERNAL;
                            }

                            info.dateFormat = entry.dateFormat();
                            info.charset = entry.charset();

                            // 找到body参数的类型和位置
                            for (int p = 0; p < paramNames.length; p++) {
                                if (paramNames[p].equals(info.bodyParamName)) {
                                    info.bodyParamType = info.paramTypes[p];
                                    info.bodyParamIndex = p;
                                }
                            }
                            // @Entry定义了body属性，但跟任何变量名都不匹配
                            if (!info.bodyParamName.isEmpty() && info.bodyParamType == null) {
                                throw new IllegalArgumentException("body attribute " + info.bodyParamName
                                        + " does not match any parameter of method "
                                        + ReflectUtils.desc(m, info.paramNames));
                            }
                            // 设置最大线程控制数
                            if (entry.maxThreadNum() > 0) {
                                info.maxThreadNum = entry.maxThreadNum();
                            } else {
                                LOG.error(info.type.name() + " " + path
                                        + ", maxThreadNum is less than 0, now reset it as default");
                                info.maxThreadNum = 100;
                            }
                            info.threadCount = new AtomicInteger(0);
                            path = info.type.name() + ' ' + path;
                            addMapping(path, info);
                            LOG.info("Map " + path + " ==> " + ReflectUtils.desc(m, paramNames));
                        }
                    }
                }
            } catch (Exception ex) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Failed to parse annotation @" + ReflectUtils.dropPkg(annotation.getTypeName()) + " on "
                            + (m == null ? (classFile + "." + methodInfo) : ReflectUtils.desc(m, paramNames))
                            + ex.getMessage(), ex);
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
