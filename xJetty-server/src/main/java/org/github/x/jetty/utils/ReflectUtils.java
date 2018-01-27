package org.github.x.jetty.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtils {
	public static Class<?>[] paramTypes(String methodSignature) throws ClassNotFoundException {
		String sign = methodSignature.substring(1);
		List<Class<?>> paramTypes = new ArrayList<Class<?>>();

		int arrayDeep = 0; // 数组层次
		for (;;) {
			switch (sign.charAt(0)) {
			case 'B':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Byte.TYPE);
					break;
				case 1:
					paramTypes.add(byte[].class);
					break;
				case 2:
					paramTypes.add(byte[][].class);
					break;
				case 3:
					paramTypes.add(byte[][][].class);
					break;
				case 4:
					paramTypes.add(byte[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'C':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Character.TYPE);
					break;
				case 1:
					paramTypes.add(char[].class);
					break;
				case 2:
					paramTypes.add(char[][].class);
					break;
				case 3:
					paramTypes.add(char[][][].class);
					break;
				case 4:
					paramTypes.add(char[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'D':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Double.TYPE);
					break;
				case 1:
					paramTypes.add(double[].class);
					break;
				case 2:
					paramTypes.add(double[][].class);
					break;
				case 3:
					paramTypes.add(double[][][].class);
					break;
				case 4:
					paramTypes.add(double[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'F':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Float.TYPE);
					break;
				case 1:
					paramTypes.add(float[].class);
					break;
				case 2:
					paramTypes.add(float[][].class);
					break;
				case 3:
					paramTypes.add(float[][][].class);
					break;
				case 4:
					paramTypes.add(float[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'I':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Integer.TYPE);
					break;
				case 1:
					paramTypes.add(int[].class);
					break;
				case 2:
					paramTypes.add(int[][].class);
					break;
				case 3:
					paramTypes.add(int[][][].class);
					break;
				case 4:
					paramTypes.add(int[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'J':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Long.TYPE);
					break;
				case 1:
					paramTypes.add(long[].class);
					break;
				case 2:
					paramTypes.add(long[][].class);
					break;
				case 3:
					paramTypes.add(long[][][].class);
					break;
				case 4:
					paramTypes.add(long[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'S':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Short.TYPE);
					break;
				case 1:
					paramTypes.add(short[].class);
					break;
				case 2:
					paramTypes.add(short[][].class);
					break;
				case 3:
					paramTypes.add(short[][][].class);
					break;
				case 4:
					paramTypes.add(short[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'Z':
				switch (arrayDeep) {
				case 0:
					paramTypes.add(Boolean.TYPE);
					break;
				case 1:
					paramTypes.add(boolean[].class);
					break;
				case 2:
					paramTypes.add(boolean[][].class);
					break;
				case 3:
					paramTypes.add(boolean[][][].class);
					break;
				case 4:
					paramTypes.add(boolean[][][][].class);
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(1);
				arrayDeep = 0;
				break;
			case 'L':
				int i = sign.indexOf(';');
				String str = sign.substring(1, i);
				str = str.replace('/', '.');
				Class<?> c = Class.forName(str);
				switch (arrayDeep) {
				case 0:
					paramTypes.add(c);
					break;
				case 1:
					paramTypes.add(Array.newInstance(c, 1).getClass());
					break;
				case 2:
					paramTypes.add(Array.newInstance(c, 1, 1).getClass());
					break;
				case 3:
					paramTypes.add(Array.newInstance(c, 1, 1, 1).getClass());
					break;
				case 4:
					paramTypes.add(Array.newInstance(c, 1, 1, 1, 1).getClass());
					break;
				default:
					throw new IllegalArgumentException("Array too many nested." + methodSignature);
				}
				sign = sign.substring(i + 1);
				arrayDeep = 0;
				break;
			case '[':
				arrayDeep++;
				sign = sign.substring(1);
				break;
			case ')':
				return paramTypes.toArray(new Class[paramTypes.size()]);
			default: // 遇到不认识的签名字符
				throw new IllegalArgumentException(
						"Unknown signature char '" + sign.charAt(0) + "'." + methodSignature);
			}
		} // end for(;;)

	}

	public static String desc(Method m, String[] paramNames) {
		try {
			StringBuilder sb = new StringBuilder();
			int mod = m.getModifiers() & Modifier.methodModifiers();
			if (mod != 0) {
				sb.append(Modifier.toString(mod)).append(' ');
			}
			sb.append(getTypeName(m.getReturnType())).append(' ');
			sb.append(getTypeName(m.getDeclaringClass())).append('.');
			sb.append(m.getName()).append('(');
			Class<?>[] params = m.getParameterTypes();
			for (int j = 0; j < params.length; j++) {
				sb.append(getTypeName(params[j]));
				sb.append(' ');
				if (paramNames != null && paramNames.length > j) {
					sb.append(paramNames[j]);
				} else {
					sb.append("arg" + j);
				}
				if (j < (params.length - 1)) {
					sb.append(',');
				}
			}
			sb.append(')');
			Class<?>[] exceptions = m.getExceptionTypes();
			if (exceptions.length > 0) {
				sb.append(" throws ");
				for (int k = 0; k < exceptions.length; k++) {
					sb.append(exceptions[k].getName());
					if (k < (exceptions.length - 1)) {
						sb.append(',');
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return "<" + e + ">";
		}
	}

	public static String getTypeName(Class<?> type) {
		if (type.isArray()) {
			Class<?> cl = type;
			int dimensions = 0;
			while (cl.isArray()) {
				dimensions++;
				cl = cl.getComponentType();
			}
			StringBuffer sb = new StringBuffer();
			sb.append(cl.getName());
			for (int i = 0; i < dimensions; i++) {
				sb.append("[]");
			}
			return sb.toString().replaceAll("java.lang.", "");
		}
		return type.getName().replaceAll("java.lang.", "");
	}

	public static String dropPkg(String className) {
		int i = className.lastIndexOf('.');
		if (i != -1) {
			return className.substring(i + 1);
		} else {
			return className;
		}
	}
}
