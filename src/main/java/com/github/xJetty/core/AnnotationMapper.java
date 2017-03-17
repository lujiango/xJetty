package com.github.xJetty.core;

import org.apache.log4j.Logger;

import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import com.github.xJetty.annotation.Entry;
import com.github.xJetty.annotation.Register;
import com.github.xJetty.annotation.ZKConf;
import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.FieldAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationObjectDiscoveryListener;

public class AnnotationMapper {
	private static final Logger LOG = Logger.getLogger(AnnotationMapper.class);

	static void process() {
		Discoverer discover = new ClasspathDiscoverer();
		discover.discover(true, true, true, true, true, true);
	}

	static class MyClassAnnotationObjectDiscoveryListener implements
			ClassAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Entry.class.getName() };
		}

		public void discovered(ClassFile clazz, Annotation annotation) {

		}

	}

	static class MyMethodAnnotationObjectDiscoveryListener implements
			MethodAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Register.class.getName() };
		}

		public void discovered(ClassFile clazz, MethodInfo method,
				Annotation annotation) {

		}

	}

	static class MyFieldAnnotationObjectDiscoveryListener implements
			FieldAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { ZKConf.class.getName() };
		}

		public void discovered(ClassFile clazz, FieldInfo field,
				Annotation annotation) {

		}

	}

}
